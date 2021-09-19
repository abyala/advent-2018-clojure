# Day 18: Settlers of The North Pole

* [Problem statement](https://adventofcode.com/2018/day/18)
* [Solution code](../src/advent_2018_clojure/day18.clj)

---

This was another very straightfoward problem, since with Advent Of Code we've already done several Game Of Life
problems. I'm lucky to have written a few convenient utility libraries in previous years, and I got to add some new
code to those namespaces too, so it felt gratifying to build up my own little library for AoC!

---

## Part 1

### Parsing the board

We are given a map of a 50x50 acre lumber collection area, and each turn each element on the grid has an opportunity
to change shape. I again prefer maps over vectors of vectors, usually because as I start these problems I never know
if the board is going to grow beyond its original size, in which case vectors become inconvenient.

My goal, like with [day 17](day17.clj), was to end up with a simple map of `{[x y] t}` where `t` is either `:open`,
`:tree`, or `:lumberyard`. While there's nothing stopping me from just storing the character values straight from the
map, I prefer to convert them into more meaningful keywords, with an option to convert them back if I need to print
out the map. In previous seasons I wrote a function `parse-to-char-coords` which does most of what we need - read a
multi-line input string and return a map of each `[x y]` pair to its contents. To convert the characters into their
keywords, I reuse the `update-values` function, which applies a function to every value in a map. In true Clojure
style, the best mapping function to apply is a simple map; who needs lambda when a map is both a data structure and
a mathematical function!

```clojure
(def map-characters {\. :open, \| :tree, \# :lumberyard})

(defn parse-input [input]
  (-> input
      (utils/parse-to-char-coords)
      (utils/update-values map-characters)))
``` 

While not specifically part of the solution, I did want to make another `print-board` function that could take my
map of coordinates to their value, and print them out again. But in this case, I want the original character values,
not the keywords. Since this was something I did on Day 17 as well, I decided to add a `print-grid` function to my
`point` namespace. This function iterates through each character of each line, printing them out one line at a time,
and applying a mapping function to each character. To map each keyword to its original character value, I used the
`map-invert` function from the `clojure.set` namespace, which swaps the keys and values within a map. Thus we can
map characters to keywords and back again with ease.

```clojure
(defn print-board [board] (point/print-grid board (set/map-invert map-characters)))

; From the advent-2018-clojure.point namespace
(defn print-grid [board drawing-fn]
  (let [lines (->> (group-by (comp second first) board)
                   (sort-by first)
                   (map second))
        line-strings (map (fn [line] (->> (sort-by ffirst line)
                                          (map (comp drawing-fn second))
                                          (apply str)))
                          lines)]
    (println (str/join "\n" line-strings))))
```

### Planning each cell's behavior

On any generation, each cell on the grid looks at its neighbors and then possibly transforms into something else. We
only need to look at the eight surrounding neighbors, so I added a simple function `surrounding-points` to the `point`
namespace. And because I was feeling particularly Math-y, why not pair together `x` and `x'`, and `y` and `y'`?

```clojure
; From the advent-2018-clojure.point namespace
(defn surrounding-points [[x y]]
  (for [y' (map #(+ y %) [-1 0 1])
        x' (map #(+ x %) [-1 0 1])
        :when (or (not= x x') (not= y y'))]
    [x' y']))
```

With that available, I want to return a map of how many times each board type appears in the characters surrounding a
point. That should be simple with the `frequencies` function, but we do need to prepare to handle points along the
perimeter, since no cell exists above the top-move cell. Simple enough. The `keep` function strips away all `nil`
values from mapping a function to a sequence, so we'll just grab the cell at a given point, keep it if it's present,
and then call `frequencies`. Finally, since I want the map to always provide all three values so I can avoid dealing
with `nil` values in the future, I'll map those frequencies on top of a default map from each cell type to `0`.

```clojure
(def no-cells-of-each-type {:open 0, :tree 0, :lumberyard 0})
(defn cell-at [board coords] (board coords))

(defn neighbors [board coords]
  (merge no-cells-of-each-type
         (->> (point/surrounding-points coords)
              (keep #(cell-at board %))
              frequencies)))
```

Now that we know our neighbors, let's do the transformation logic with a `cell-next-turn` function, which takes in
a board and the coordinates, and returns what the cell will be next turn. I won't go line-by-line through this, but
it gets the neighbor frequencies, does a `case` statement on the current cell at those coordinates, and then does some
simple `if` statements to return its next value.

```clojure
(defn cell-next-turn [board coords]
  (let [n (neighbors board coords)]
    (case (cell-at board coords)
      :open (if (>= (:tree n) 3) :tree :open)
      :tree (if (>= (:lumberyard n) 3) :lumberyard :tree)
      :lumberyard (if (and (>= (:lumberyard n) 1)
                           (>= (:tree n) 1))
                    :lumberyard :open))))
```

### Finishing the program

We've got all of the building blocks we need to finish up. I like to make functions that show a single change in state
for these problems, rather than doing both the looping and the transformation logic in a single function, so we'll
build the `next-turn` function now. The goal is to grab each element of the `board`, which again is `{[x y] t}`,
determine the value of the cell at those coordinates in the next go-round, and then associate them to the resulting
map.

The `reduce-kv` core function does all of this for us out of the box! Besides taking in the initial reduce value
(an empty map) and the data to reduce over (the board itself), it takes a mapping function with arguments of the 
reduced value, and the key and value from the map. Figure out what shows up on the new coordinates and associate them
to the new map, and we've got ourselves a nice little function.

```clojure
(defn next-turn [board]
  (reduce-kv (fn [next-board coords _] (assoc next-board coords (cell-next-turn board coords)))
             {}
             board))
```

To finish solving the problem, we'll need to take the final state of the board and multiple together the number of
trees by the number of lumberyards. Once again, I'll merge the actual frequencies over the map of each cell type to
0, so we can handle nulls gracefully. Thus we take the board, strip out the values since we don't care where each 
cell is, calculate the frequencies, and then multiply the values together.

```clojure
(defn resource-value [board]
  (let [freqs (merge no-cells-of-each-type (-> board vals frequencies))]
    (* (:tree freqs) (:lumberyard freqs))))
```

Let's finish it!  We need to calculate the resource value on the 10th generation, so we'll iterate on the `next-turn`
function, use `nth` to grab the 10th value, and then call `resource-value`. Nice and easy.

```clojure
(defn part1 [input]
  (-> (iterate next-turn (parse-input input))
      (nth 10)
      (resource-value)))
```

---

## Part 2

Now we need to calculate the resource value on the billionth generation, and I don't have time to wait around and see
how that works out. It's clear from the phrasing that at some point, the boards are going to loop, as Game Of Life
problems eventually do on a finite board. So we first need to figure out when that happens.

The `find-board-loop` sets up a `reduce` function that eventually terminates with a `reduced` response. This could just
as easily have been a `loop-recur`, but I'm feeling functional today. To make this infinite, I'll just power it with
an infinite `range` sequence, and for each reduction I'll work with a map that represents each board twice - once in a
vector so we can easily look up a board by its index, and as a map from the board to the index in which we'll find it
in the map. I suppose I could have just used a single vector and did linear scanning, but I didn't know how long it
would take to do the scanning, so a constant-time hash map seemed nice.

In each iteration, we calculate the `next-turn` form of the board, feeding in the last board already seen. Again, as a
tiny optimization, I used `(boards generation)` instead of `(last boards)` since we always want the last one, and I
believe `last` on a vector runs in linear time. If we have already seen the next generation's board in the past, we've
got a loop and can exit. I chose the exit value to be another map with the `:boards` vector and the `:loop-idx` with 
the index that should appear after the last one in the vector. If we haven't seen the next board, `conj` it to the end
of the boards, and associate the board to its index in the `:seen` map.

```clojure
(defn find-board-loop [initial-board]
  (reduce (fn [{:keys [boards seen]} generation]
            (let [next-board (next-turn (boards generation))]
              (if-let [original-index (seen next-board)]
                (reduced {:boards boards, :loop-idx original-index})
                {:boards (conj boards next-board), :seen (assoc seen next-board (inc generation))})))
          {:boards [initial-board], :seen {initial-board 0}}
          (range)))
```

Now that we have a map of `{:boards [], :loop-idx i}`, we need to determine which board exists at a certain generation.
For completeness, I handle what happens if you pass in a `target` generation before we loop, but that doesn't apply
here. The rest is just a little math -- since we're looping, start with the `loop-idx` amount and subtract that from
the total `target` value to see how far into the loop we need to go. Then mod that loop amount from the _size_ of the
loop.  Add those two values together, and grab the board at that index.

```clojure
(defn board-at-index [boards loop-idx target]
  (if (< target (count boards))
    (boards target)
    (let [loop-size (- (count boards) loop-idx)]
      (boards (+ loop-idx
                 (mod (- target loop-idx) loop-size))))))
```

Finally, we put the pieces together. We'll find the board loop from the initial board, and immediately destructure it.
Then we get the target board at the index 1 billion. Finally, we calculate the resource value.

```clojure
(defn part2 [input]
  (let [{:keys [boards loop-idx]} (find-board-loop (parse-input input))
        target-board (board-at-index boards loop-idx 1000000000)]
    (resource-value target-board)))
```

Very nice, straightforward problem.  Thanks, Advent!