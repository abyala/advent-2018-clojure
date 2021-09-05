# Day 16: Chronal Classification

* [Problem statement](https://adventofcode.com/2018/day/16)
* [Solution code](../src/advent_2018_clojure/day16.clj)

---

This problem involved a computer that doesn't know which instruction to apply when, so we had to get it past its little
identity problem. Overall, this was a very straightforward problem I round rather relaxing and fun.

---

## Part 1

### Defining the operations

The instructions describe a computer that has four registers, and series of instructions of the form `[op a b c]`,
followed by a description of various processing algorithms.  It would have been very simple to make 16 simple functions
as below:

```clojure
(defn addr [reg a b c] (assoc reg c (+ (reg a) (reg b))))
(defn addi [reg a b c] (assoc reg c (+ (reg a) b)))
; ... and so on
``` 

But of course, that's far too easy and obvious, and I felt like being a little silly by composing functions. This most
definitely didn't make things easier to read, so don't try this at home, kids.  Anyway, we can see that many of the
operations are very similar -- `addr` adds two registers, `addi` adds a register and a value, `mulr` multiplies two
registers, and `muli` multiplies a register and a value. So with that, we can say that there are two _types_ of
functions - those that apply to two registers, and those that apply to a register and a value, which the problem 
statement calls an "immediate" operation.  Therefore, the first eight functions perform addition, multiplication,
bitwise and, and bitwise or for both operation types.

```clojure
(defn register-op [op reg a b] (op (reg a) (reg b)))
(defn immediate-op [op reg a b] (op (reg a) b))
(def addr (partial register-op +))
(def addi (partial immediate-op +))
(def mulr (partial register-op *))
(def muli (partial immediate-op *))
(def banr (partial register-op bit-and))
(def bani (partial immediate-op bit-and))
(def borr (partial register-op bit-or))
(def bori (partial immediate-op bit-or))
```

Then the next two functions are simply enough that there's nothing to genericise - `setr` just uses a register value
and `seti` just uses a literal value, with neither of them looking at argument `b`.

```clojure
(defn setr [reg a _] (reg a))
(defn seti [_ a _] a)
``` 

The last six operations set a value to either 0 or 1 based on comparison testing, where the arguments `a` and `b` 
are treated as either registers or immediate values. So `compare-op-imm-reg`, `compare-op-reg-imm`, and
`compare-op-reg-reg` compare the registers as appropriate, and call into `compare-op` to return either the 0 or 1.

```clojure
(defn compare-op [op a b] (if (op a b) 1 0))
(defn compare-op-imm-reg [op reg a b] (compare-op op a (reg b)))
(defn compare-op-reg-imm [op reg a b] (compare-op op (reg a) b))
(defn compare-op-reg-reg [op reg a b] (compare-op op (reg a) (reg b)))
(def gtir (partial compare-op-imm-reg >))
(def gtri (partial compare-op-reg-imm >))
(def gtrr (partial compare-op-reg-reg >))
(def etir (partial compare-op-imm-reg =))
(def etri (partial compare-op-reg-imm =))
(def etrr (partial compare-op-reg-reg =))
```

Finally, I created a simple vector of `all-operations` that assembled all of the operations. A map would be nicer,
but we don't ever actually use the names of the functions, so assigning them to strings or symbols isn't stricly needed.

```clojure
(def all-operations [addr addi mulr muli banr bani borr bori setr seti gtir gtri gtrr etir etri etrr])
```

Again, this is isn't worth the complexity, but I enjoyed using the higher order functions regardless.

### Parsing the input

I wouldn't normally bring a bunch of attention to string parsing, but this time I leveraged the 
[edn parser](https://github.com/edn-format/edn) that's built in to Clojure, since the incoming data was almost
perfectly suited.

First, let's assume we've got a triple line that represents a test sample. The first line gives the initial state of
the registers; the second line has the four values of `op`, `a`, `b`, and `c`; and the third line provides the final
state of the registers. I want to represent the sample as a simple map with fields `:before :op :a :b :c :after`, where
`:before` and `:after` are vectors of their registers. Thankfully, the input provides a format of `[3, 2, 1, 1]`,
which parses through edn into a vector of four integers, so that's great. Thus with a little regex, we get the
`parse-sample` function, and `parse-samples` just maps each group that's separated by a newline to its sample:

```clojure
(defn parse-sample [sample]
  (let [[_ before op a b c after] (re-find #"Before: (\[.*\])\n(\d+) (\d+) (\d+) (\d+)\nAfter:  (.*)" sample)]
    {:before (edn/read-string before)
     :op     (Integer/parseInt op)
     :a      (Integer/parseInt a)
     :b      (Integer/parseInt b)
     :c      (Integer/parseInt c)
     :after  (edn/read-string after)}))

(defn parse-samples [input]
  (->> (str/split input #"\n\n")
       (map parse-sample)))
```

After the three blank lines, we get to the test data, which we'll need in part 2. I could read each line into a map
again, and that probably would have been nice, but I enjoyed working with edn. Now the string `"8 2 0 0"` would become
a string instead of a vector of four integers, so instead I manually added square brackets around the line and then
asked edn to do its parsing. Worked just fine for the `parse-test-program` function, and then the `parse-input` 
function just reads all of the data and provides a map of the `samples` and `test-program` data. Note that, as with
other problems, I had issues with Windows adding `\r` to the `\n`, so my `dos2unix` function cleaned that up.

```clojure
(defn parse-test-program [input]
  (->> (str/split-lines input)
       (map #(edn/read-string (str "[" % "]")))))

(defn parse-input [input]
  (let [[samples test-program] (str/split (utils/dos2unix input) #"\n\n\n\n")]
    {:samples (parse-samples samples)
     :test-program (parse-test-program test-program)}))
```

### Actual Part 1 Logic

Ok, that was a lot of setup, so now it's time for the meat of the problem. For each sample, we want to know how many
have 3 or more operations that return the `After` value given the `Before` input. So first we'll make a simple
`run-operation` function that updates register `c` to a vector of `registers`, given an `op` and inputs `a` and `b`.
The `op` in question will be one of the 16 we prepared earlier, so this is a simple use of `assoc`.

```clojure
(defn run-operation [registers op a b c]
  (assoc registers c (op registers a b)))
```

Next we want to make a function called `operator-matches`, which takes in a sample and returns the set of all operators
that provide the expected `after` result. Now whenever an Advent Of Code problem deals with "program" projects, we know
that eventually we'll be plugging in different operations, so I'm going to overload this function to except either
just the `sample`, or the `sample` and the `ops` we want to test with a default of `all-operations`. This isn't so bad
now - filter out each of the `ops` where `run-operation` returns the `after` value we expect, and create a set.

```clojure
(defn operator-matches
  ([sample] (operator-matches sample all-operations))
  ([sample ops] (let [{:keys [before _ a b c after]} sample]
                  (->> ops
                       (filter #(= after (run-operation before % a b c)))
                       set))))
```

Finally, we can write the `part1` function. Here we parse the input, extract out just the samples, and convert each
sample into the set of operators that match. With a sequence of sets, we filter for only those with a cardinality of
at least 3, and count up the number of samples.  Easy enough!

```clojure
(defn part1 [input]
  (->> (parse-input input)
       :samples
       (map operator-matches)
       (filter #(>= (count %) 3))
       count))
```

---

## Part 2

Unsurprinsgly, we need to figure out which operator runs which function, and then we must execute the test program.
There aren't many surprises here.

First, let's define `initial-solve-opcodes` to make a single pass through all of the rules using a `reduce` function.
We'll start with a map of all of the operator IDs (numbers 0 through 15) mapped to each of the possible operators.
There's a really nice syntax to do this part, where we begin with a sequence of two-element vectors that represent
each key-value pair, and we then put them into an empty map with the `into` function. This process always trips me up
a bit, but the relevant portion of this function looks like this:

```clojure
(into {} (map vector (range 16) (repeat (set all-operations))))
```

I always try to use the format `(map f col)` to map a single function onto a collection, but this time we will use the
format `(map f c1 c2)`, which applies `f` to the arguments coming out of collections `c1` and `c2` together. Thus the
first round will return `(f (first c1) (first c2))` followed by `(f (second c1) (second c2))` until one collection runs
empty. We'll use `(range 16)` to get the possible opcodes, and `(repeat (set all-operations))` to return an infinite
sequence of sets of possible functions. Very nice.

With that out of the way, we can write the `reduce` function. As we go through each of the `samples`, we update
`possibilities` map from above and filter them down by the `operator-matches` that work for the given sample. Thus we
may start with 16 operators and end up with only 2 or 3 after going through each sample. When the `reduce` function
finishes, we'll end up with each opcode mapping to all of the functions that work for all of the samples.

Alternatively we could have taken each sample, mapped it to a vector of `[opcode possibilities]`, which we'd later
group and take the set intersection, but I liked this model better since it did the work without extra steps.

```clojure
(defn initial-solve-opcodes [samples]
  (reduce (fn [possibilities sample] (update possibilities (:op sample) #(operator-matches sample %)))
          (into {} (map vector (range 16) (repeat (set all-operations))))
          samples))
```

Now that we've done the initial analysis, we need to iterate until every opcode has been assigned its function. To do
this, we'll write `solve-opcodes` which looks at each opcode, pulling out each one that has only one possible function.
Once we find such a match, we remove that function from all other opcodes and iterate. This is a nice case for a
`loop-recur` structure, and the helper function `algorithm-known?` that checks if an opcode has only one value in its
set.

```clojure
(defn algorithm-known? [[_ algs]]
  (= 1 (count algs)))

(defn solve-opcodes [samples]
  (loop [unsolved (initial-solve-opcodes samples) solved {}]
    (if-let [[id algs] (first (filter algorithm-known? unsolved))]
      (let [alg (first algs)]
        (recur (-> (dissoc unsolved id)
                   (utils/update-values #(disj % alg)))
               (assoc solved id alg)))
      solved)))
```

Assuming that all works (it does!), we can define `run-program`, which takes in the completed opcode map and a sequence
of instructions as the `test-program`, and returns the resulting vector of registers. Once again, `reduce` fits the
bill just fine, and we can use the `run-operation` function that powered `operator-matches` up above.

```clojure
(defn run-program [opcodes test-program]
  (reduce (fn [registers [op a b c]] (run-operation registers (opcodes op) a b c))
          [0 0 0 0]
          test-program))
```

Finally, we throw it all together with the `part2` function - parse the input, solve the opcodes, run the program,
and return the first register. How beautiful is that!

```clojure
(defn part2 [input]
  (let [{:keys [samples test-program]} (parse-input input)
        opcodes (solve-opcodes samples)
        registers (run-program opcodes test-program)]
    (first registers)))
```