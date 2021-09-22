# Day 19: Go With The Flow

* [Problem statement](https://adventofcode.com/2018/day/19)
* [Solution code](../src/advent_2018_clojure/day19.clj)

---

This problem was a mixed bag -- the first part was a lot of fun to build off the device program from Day 16, while the
second part was one of those incredibly annoying problems where you have to stare at the problem and hope you figure
out the clever trick. As I have no interest in those puzzles, I Googled the approach for part 2 and then implemented 
the code myself.

---

## Part 1

The bulk of this problem involved extracting logic out of the Day 16 code into a new namespace, called `wrist-device`.
Every time I used that term, I thought of the healing and transportation devices from Stargate SG-1, because I'm a
giant nerd. So let's start there before we work on Day 19 and refactor Day 16.

### Wrist Device namespace - the basics 

Building the `write-device` namespace included moving over all of the 16 operations from Day 16, and putting all of
them into a map called `operations`, which mapped each keyword to its function. I thought this is an interesting way to
build the map, since I named each function by its four-letter abbreviate. Rather than creating the map that manually
binds each keyword to its function, like `{:addr addr, :addi addi, :mulr mulr...}`, I did it programmatically. Starting
with a vector each function's _symbol_, effectively its pointer, I mapped each value from the keyword of its symbol to
the to the evaluated function it represents. Try doing that in Java!

After that, I provided two utility functions.  `operation-ids` just returns the keys from the `operations` map, which
is a sequence of those keywords.  And `operation-named` looks up an `operation` by its keyword name.

```clojure
(defn register-op [op reg a b] (op (reg a) (reg b)))
(defn immediate-op [op reg a b] (op (reg a) b))
(def addr (partial register-op  +))
(def addi (partial immediate-op +))
(def mulr (partial register-op  *))
(def muli (partial immediate-op *))
(def banr (partial register-op  bit-and))
(def bani (partial immediate-op bit-and))
(def borr (partial register-op  bit-or))
(def bori (partial immediate-op bit-or))
(defn setr [reg a _] (reg a))
(defn seti [_ a _] a)
(defn compare-op [op a b] (if (op a b) 1 0))
(defn compare-op-imm-reg [op reg a b] (compare-op op a (reg b)))
(defn compare-op-reg-imm [op reg a b] (compare-op op (reg a) b))
(defn compare-op-reg-reg [op reg a b] (compare-op op (reg a) (reg b)))
(def gtir (partial compare-op-imm-reg >))
(def gtri (partial compare-op-reg-imm >))
(def gtrr (partial compare-op-reg-reg >))
(def eqir (partial compare-op-imm-reg =))
(def eqri (partial compare-op-reg-imm =))
(def eqrr (partial compare-op-reg-reg =))

(def operations (into {} (map #(vector (keyword %) (eval %))
                              ['addr 'addi 'mulr 'muli 'banr 'bani 'borr 'bori
                               'setr 'seti 'gtir 'gtri 'gtrr 'eqir 'eqri 'eqrr])))
(def operation-ids (keys operations))
(defn operation-named [name] (operations name))
```

Next, I created a factory function to create a wrist device, using Clojure's equivalent of optional function arguments.
The `create-device` function always takes in the initial registers, as well as the vector of instructions it
will process. Then any additional values, expressed as an ampersand to mean `rest`, can be treated as a map of values.
The function supports two optional parameters - `operations`, a mapping of how each operation name is associated to its
implemented function, and `ip-register`, the new concept in Day 19. Because the `rest` arguments come in as a vector,
Clojure can treat it as a map with `:keys`, which it can then `or` with default values. In this case the `operations`
map I defined above and a `nil` `ip-register` for when it's not needed. Note that my overloaded use of `operations` is
quite terrible.

```clojure
(defn create-device [registers instructions & {:keys [operations ip-register]
                                               :or {operations operations ip-register nil}}]
  {:registers registers
   :instructions instructions
   :operations operations
   :ip-register ip-register
   :ip 0
   :halted? false})
```
 
Then again, I added a few more convenience functions to make the code read a little better. Nothing fancy here.

```clojure
(defn registers [device] (:registers device))
(defn ip [device] (:ip device))
(defn ip-register [device] (:ip-register device))
(defn halted? [device] (:halted? device))
(defn get-op [device op] (get-in device [:operations op]))
```
 
### Wrist Device namespace - the new stuff

I consider the concept of the instruction pointer to be part of the Wrist Device, not necessarily the Day 19 solution,
since this is enhancing how the device works, not necessarily what our problem itself is solving.  So let's keep going.

First off, we introduce the concept of the instruction pointer (or `ip`) and program halting. The `ip` starts at zero
and points to the next `instruction` the program will run. If it ever reaches a value outside the `instruction` vector,
the program halts and won't run any more operations. To achieve this, we start with the function
`inc-instruction-pointer`, which both increments the `ip` within the device and sets `:halted?` to true if it doesn't
find a next instruction to run.

```clojure
(defn inc-instruction-pointer [device]
  (let [new-ip (inc (ip device))]
    (assoc device :ip new-ip
                  :halted? (nil? (get-in device [:instructions new-ip])))))
```

Next we have the functions `load-instruction-pointer` and `store-instruction-pointer`, which, if the device has a valid
`ip-register`, will respectively push the value of the `ip` into the `ip-register`or stores the register value 
back into the `ip`. The `if-let` function will only modify the device if an `ip-register` exists, or else the function
returns the device unchanged.

```clojure
(defn load-instruction-pointer [device]
  (if-let [reg (ip-register device)]
    (assoc-in device [:registers reg] (ip device))
    device))

(defn store-instruction-pointer [device]
  (if-let [reg (ip-register device)]
    (assoc device :ip (get-in device [:registers reg]))
    device))
```

The function `run-operation-at-instruction-pointer` is basically the old `run-operation` from the original Day 16 code,
given that the device now holds more than just registers. All it does it read the instruction at the current `ip`
index, destructures it, and then associates the new value at register `c` by finding the correct operation and applying
it to the registers with arguments `a` and `b`. I originally wrote this as an `update` function to get multiple uses
of the registers, but I found that solution to be uglier.

```clojure
; This is the function I like
(defn- run-operation-at-instruction-pointer [device]
  (let [[op a b c] (get-in device [:instructions (ip device)])
        regs (registers device)]
    (assoc-in device [:registers c] ((get-op device op) regs a b))))

; And this is the original function with the "update." The assoc-in is just easier to read. 
(defn- run-operation-at-instruction-pointer [device]
  (let [[op a b c] (get-in device [:instructions (ip device)])]
    (update device :registers #(assoc % c ((get-op device op) % a b)))))
```

Two functions left -- one to execute a single operation, and then one to run the full program. To run a single
operation, in the function `run-operation`, we first ensure the program hasn't halted yet. If it hasn't, then we go
through a nice set of tiny functions in order: from the device, load the `ip` (if appropriate),
run the operation where it currently sits, store the `ip` (if appropriate), and increment the `ip`. This, to me,
is the functional programming at its best.

Then to run the program until it halts, we infinitely call `run-operation` from the original device, wait until it
halts, and then return the first device value back.

```clojure
(defn run-operation [device]
  (if-not (halted? device)
    (-> device
        load-instruction-pointer
        run-operation-at-instruction-pointer
        store-instruction-pointer
        inc-instruction-pointer)
    device))

(defn run-to-completion [device]
  (->> (iterate run-operation device)
       (filter halted?)
       first))
```

With a working wrist device, let's go solve Day 19!

## Part 1 (day 19 namespace)

The data set we read defines an `ip-register` once in the very first line, and then just has a list of instructions.
So after defining that a Day 19 device has 5 registers (one for the `ip-register` and then the regular 4 others), we
parse the data. `parse-instruction` takes a single line, splits it by spaces, and keeps the keyword version of the `op`
and the integer versions of the others. Since the default mapping of operations in the wrist device is from its keyword
to its function, `(keyword op)` lines us up nicely.

```clojure
(def register-count 5)

(defn parse-instruction [text]
  (let [[op a b c] (str/split text #" ")]
    [(keyword op) (Integer/parseInt a) (Integer/parseInt b) (Integer/parseInt c)]))
```

Parsing the entire file takes a tiny bit of extra work. We split the input data by line, destructuring those lines
into the first line (`ip-text`) and the sequence of the rest of the remaining lines (`& instruction-text`). This is our
second use of the ampersand to represent "the rest of the arguments," but this time in a return type destructuring
instead of a function argument destructuring. The `ip-register` declaration line is of form "#ip 4", so we just parse
that substring of the first line.

To create the device, we'll provide the constant register count of 5, and `mapv` the instructions by calling
`parse-instruction` on each line; note that we need `mapv` since we access instructions by index. Then we don't need
to provide the optional `:operations` argument, but we do want to provide the `ip-register` value we read from the
input, so here we leverage the optional arguments we implemented in `create-device`. Putting it all together, we get
a simple `parse-device` function:

```clojure
(def empty-registers [0 0 0 0 0])

(defn parse-device [input]
  (let [[ip-text & instruction-text] (str/split-lines input)
        ip-register (Integer/parseInt (subs ip-text 4))]
    (device/create-device empty-registers
                          (mapv parse-instruction instruction-text)
                          :ip-register ip-register)))
```

Now to finally solve part 1, we parse the incoming data, ask the device to run to completion, extract out its
registers, and read the first value. It makes it look like this problem took 5 minutes now that everything's over in
the `wrist-device` namespace, doesn't it?

```clojure
(defn part1 [input]
  (->> (parse-device input)
       (device/run-to-completion)
       (device/registers)
       first))
```

That was a ton of fun!  Which brings us to...

---

## Part 2

No more ranting about this problem. Apparently, the goal is to run the program for just a few cycles, since it'll take
an extremely long time to run fully. Find the very large number, determine all of the values that divide into it, and
add those values together. So... I did that.

```clojure
(defn part2 [n]
  (let [divisors (filter #(zero? (mod n %))
                         (range 1 (inc n)))]
    (apply + divisors)))
```

Let's not speak about this part anymore, and instead go back to refactoring Day 16!

---

## Refactoring Day 16

Day 16 needed several changes to work properly, besides simply moving code into `wrist-device`.

First of all, `operator-matches` now uses the actual `operation-ids` that the `wrist-device` namespace defines, 
instead of using simple functions like I foolishly did the first time around. Remember that this function attempts
to answer which operators (numbers, not keyword names) convert the `before` registers into the `after` registers.
To do this, for each named operation, we create a new device with a single instruction set, the `before` values set to
the initial registers, and an `operations` map that binds the numeric operator to the operation. After running the
program a single step (we could have also called `run-operation-to-completion`), we pull the registers back out again
and compare that to the `after` vector.

```clojure
(defn operator-matches
  ([sample] (operator-matches sample device/operation-ids))
  ([sample ops] (let [{:keys [before op a b c after]} sample]
                  (->> ops
                       (filter #(= after (-> (device/create-device before
                                                                   [[op a b c]]
                                                                   :operations {op (device/operation-named %)})
                                             (device/run-operation)
                                             (device/registers))))
                       set))))
```  

The rest of Part 1 worked mostly unchanged, but we have to update both `run-program` and `part2`. The new
`run-program` solution calls `run-to-completion` now. To create the device, we pass in the `register-count` of 4 for
Day 16 and the `test-program` that we parsed, but now we provide a new operation map. The `solve-opcodes` function
still returns a map of operation name (a number from 0-15) to its operation name, so we need to update every value
in that map to its underlying function. The utility function `update-values` does this cleanly, leveraging the 
helper function `device/operation-named`. 

```clojure
(def empty-registers [0 0 0 0])

(defn run-program [opcodes test-program]
  (device/run-to-completion (device/create-device empty-registers
                                                  test-program
                                                 :operations (utils/update-values opcodes device/operation-named))))
```

Finally, since the device is a complex object, we again need to extract the first register out of the completed program,
just like we did with Day 19 Part 1, so we just thread each step in order.

```clojure
(defn part2 [input]
  (let [{:keys [samples test-program]} (parse-input input)]
    (-> (solve-opcodes samples)
        (run-program test-program)
        (device/registers)
        first)))
```