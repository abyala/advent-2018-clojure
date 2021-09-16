# Day 17: Reservoir Research

* [Problem statement](https://adventofcode.com/2018/day/17)
* [Solution code](../src/advent_2018_clojure/day17.clj)

---

I loved this problem, until I banged my head against the wall for several days since every test case I gave it passed
while I still got the wrong answer to the puzzle. Turns out there was a tiny little detail right at the end of the
instructions that I missed after reading it a dozen times.  Oh well.

The puzzle involves a water spring providing an endless stream of water into a pit of sand and clay. Water flows
through sand but cannot go through clay, but it can pool up when fully surrounded by play or other resting water. From
the puzzle input, we use the terms "sand" and "clay" for the puzzle input, "resting-water" to refer to water that's
pooled up against clay, and "flowing-water" to refer to water that cannot pool and is either flowing down or running
off an edge.

---

## Part 1

### Parsing the board

I thought of several ways to represent the data, but the most intuitive mode for me, without worrying about offsets,
was to parse the input into a simple map of `{[x y] t}` where `x` and `y` are the coordinates, and `t` is the type
of material at that point.  I created a box of all points we would need to use to run the program; `x` would be the
range from one less than the minimum clay value to one greater than the max, to allow water to spill off the edge; and
`y` would range from 0 (the water spring) down to the largest `y` value that contains clay. Note that the instructions
say to only consider `y` coordinates smaller than the smallest `y` coordinate in the scan data, but we'll come back
to that.

First, let's parse an individual line of data. It can be either of the form `x=a, y=b..c` or `y=a, x=b..c`. Either way,
we end up creating a straight line, or perhaps a single point if `(= b c)`. As I see it, we'll return a sequence of
`[x y]` vectors, where either `x` is a fixed value and `y` is a range of values, or vice versa. After applying the
regex, `fixed-range` represents an infinite sequence of values of `a`, while `moving-range` is a finite range of
values from `b` to `c`, where we need to call `(inc c)` since the `range` function is closed on the right. Finally,
`(map f r1 r2)` returns a sequence of mapping the function `f` to each matching pair of values of `r1` and `r2`, in
this case being the `vector` function.

```clojure
(defn parse-coords [line]
  (let [[_ fixed-axis fixed-val min-val max-val] (re-find #"([x|y])=(\d+), [x|y]=(\d+)\.\.(\d+)" line)
        fixed-range (repeat (Integer/parseInt fixed-val))
        moving-range (range (Integer/parseInt min-val) (inc (Integer/parseInt max-val)))]
    (case fixed-axis "x" (map vector fixed-range moving-range)
                     "y" (map vector moving-range fixed-range))))
```

Next, the `parse-input` function just parses each line of the input, and `mapcat`s (flat maps) the values together,
creating a single sequence of vector coordinates.

```clojure
(defn parse-input [input]
  (->> input str/split-lines (mapcat parse-coords)))
```

Now I need the box of all points I want to consider in the calculation.  Looking at all of the clay points identified
in `parse-input`, I'll search for the min and max values of `x` and `y`.  As explained above, I'll need one less than
the min `x`, one more than the max `x`, the value `0` for the min `y`, and the unchanged max value of `y`. To make
this a little fun, I created a helper partial function `min-max`, which takes a sequence of values and returns a vector
of its min and max value. Good old `juxt` takes in one or more partial function, and returns a function that applies
each of its internal functions to a collection; in this case `juxt` returns the applied `min` and `max` values. To
use `min-max`, we just grab the `first` values for `x` and the `second` values for `y`, and then assemble the output
map.

```clojure
(defn bounded-box [points]
  (let [min-max (juxt (partial apply min) (partial apply max))
        [min-x max-x] (min-max (map first points))
        [_     max-y] (min-max (map second points))]
    {:min-x (dec min-x),
     :max-x (inc max-x),
     :min-y 0                                               ; Min-y must always be 0 to include the spring
     :max-y max-y}))
``` 

Finally, we write the function `parse-board`, which takes in the input stream, identifies all of the clay points, and
gets the bounded box. Then we map every possible point in the bounded box to `:sand` and all of the clay points to
`:clay`, and build them all into one big map, where the clay values overwrite the sand values.  Eh viola!

```clojure
(defn parse-board [input]
  (let [clay-points (parse-input input)
        {:keys [min-x max-x min-y max-y]} (bounded-box clay-points)]
    (into {} (concat (map vector (point/all-points [min-x min-y] [max-x max-y]) (repeat :sand))
                     (map vector clay-points (repeat :clay))))))
```

### Helper functions

Before we get to the fun stuff, let's look at a few helper functions. First, it looks dumb, but I created `point-at`
to return the value of the point within the board, which I did mostly because I wasn't confident the board would work
out well as a simple map (it did), and I wanted to be able to change its structure easily. This was one place where I
was reminded that even though Clojure doesn't use OO principles per se, and "every object is a map," business functions
like this provide expressiveness that you could call encapsulation if you squint a little bit.

Anyway, I also created `move-left`, `move-right`, and `move-down` since we'll be moving from point to point.

```clojure
(defn point-at [point board]
  (board point))

(defn move-left [[x y]]  [(dec x) y])
(defn move-right [[x y]] [(inc x) y])
(defn move-down [[x y]]  [x (inc y)])
```

Additionally, I created `water?` and `stable?` functions to define the capabilities of different
types of terrain. Part 1 will require counting all points that have water, which means both flowing and resting water.
And in a moment we'll need to recognize that resting water can sit on top of clay or more resting water, but cannot
itself sit on top of either sand (it should drop) or flowing water (the flowing water would flow away), so `stable?`
defines the types on which resting water can sit..

```clojure
(defn water? [t] (#{:flowing-water :resting-water} t))
(defn stable? [t] (#{:resting-water :clay} t))
```

Finally, to help with diagnostics, I created a helper `print-board` function that isn't strictly needed, but which
prints out the current state of the board such that it uses `.`, `#`, `|`, and `~` characters, per the instructions.
This isn't the cleanest implementation, I'm sure, but it go the job done. Note that this would have been easier if
I had either expressed the board as a vector of vectors, or a vector of strings.

```clojure
(defn print-board [board]
  (let [lines (->> (group-by (comp second first) board)
                   (sort-by first)
                   (map second))
        line-strings (map (fn [line] (->> (sort-by ffirst line)
                                          (map (comp {:sand \. :clay \# :flowing-water \| :resting-water \~}
                                                     second))
                                          (apply str)))
                          lines)]
    (println (str/join "\n" line-strings))))
```

### Calculations

I'm going to skip the `neighbor-cells` function for a moment, and go straight to the most important function in the
program, `run-water`.  Rather than returning a single changed state of the board, which would have been a much smarter
move honestly, this takes a board, runs water all the way through it, and returns the resulting board state. It's not
very difficult, once broken apart.

Let's start with the basic structure, which is a loop that manages a list of water drips that need to be examined
(`drips-to-check`) and the current state of the board (`current-board`). The water starts one spot below the water
spring, and we use a Clojure list because it makes for a perfect stack by adding and removing elements from its front.
Plus that makes for a much more efficient data structure than a vector, given the workload ahead. Anyway, we will
run through this loop until there's nothing left in the `drips-to-check` list to bind to the `drip` variable, at which
point we return the `current-board`.

```clojure
(defn run-water [board]
  (loop [drips-to-check (list (move-down water-spring)) current-board board]
    (if-let [drip (first drips-to-check)]
      ;; All of the logic goes here
      current-board)))
```

For each coordinate (drip), we made decisions based on what's at that cell, what's below it, and what's beside it.
* If the point is in a final/terminating state, then pop it from the stack without changing the board. Nothing ever
overwrites clay, and once water is resting, it's resting for good. So that's our first condition.
* If water is dripping over sand, then let it drip and come back to it later. At the end of the day, this will either
remain flowing water or become resting water, and we'll see how in the next two conditions. Since we will have to
return to this cell later, push the cell below onto the tack by `cons`ing it onto the front of `drips-to-check`.
* If the cell below the current drip is flowing water, then the current cell must be flowing water that falls onto
flowing water. So just like the first condition, pop this cell using `rest` and move on.
* If the cell is otherwise flowing on top of a stable surface, as previously described by `stable?`,
then we need to examine the horizontal neighbors of this cell. If all the neighbors on both sides continue to sit on
running water until they run into clay, then all of those cells are going to hold resting water. If either direction
hits an unstable cell before reaching a stable one, then the whole row becomes flowing water. Note that in one of my
rewrites, I sacrificed a bunch of performance for the sake of readability by only associating the current cell to be
either resting or flowing water, and then pushing the candidate neighbors onto the stack to be reexamined; this means
that once we identify a row of resting water, each cell will end up in the row will recalculate the fact that it is
resting. It's silly, but readable.  And this depends on the `neighbor-cells` function we'll get to in a minute.
* Finally, if the cell sits on top of a point that's no longer on the board, we've reached the very bottom, so the
problem statement tells us is the last drop of running water along this path.

```clojure
(defn run-water [board]
  (loop [drips-to-check (list (move-down water-spring)) current-board board]
    (if-let [drip (first drips-to-check)]
      (let [below (move-down drip)
            type-at (point-at drip current-board)
            type-below (point-at below current-board)]
        (cond
          ; If this is a stable point, leave it alone.
          (stable? type-at) (recur (rest drips-to-check) current-board)

          ; If there's sand below, we don't know anything. We may be flowing water now, but later could become
          ; resting water. Best to not make any decision yet and come back later after the lower cells resolve.
          (= :sand type-below) (recur (cons below drips-to-check) current-board)

          ; If the point below is flowing water, all this cell can be is flowing water
          (= :flowing-water type-below) (recur (rest drips-to-check) (assoc current-board drip :flowing-water))

          ; If the point below is "solid" (clay or resting), then check the neighbors
          (stable? type-below) (let [{:keys [rests? candidates]} (neighbor-cells drip current-board)]
                                                 (recur (apply conj (rest drips-to-check) candidates)
                                                        (assoc current-board drip (if rests? :resting-water :flowing-water))))
          ; Only the abyss below us
          :else (recur (rest drips-to-check) (assoc current-board drip :flowing-water))))
      current-board)))
```  

So let's go back to `neighbor-cells` and examine that function. To start off, I define a lambda function called
`direction-rests?`, which will take a point to examine and a function to apply to see its next neighbor. If a point
is clay, then we've hit the last spot we need to examine, and that entire direction supports resting water. If not,
but the point below the one being inspected supports running water, then this direction might become resting, but we
need to keep looking until we hit a clay wall. Otherwise, this is an unstable point that sits above either sand or
running water, so this direction is not resting.

The function checks if `direction-rests?` is true in both directions (`move-left` and `move-right`). If so, then the
point itself is resting, so it returns a map of `rests?` being set to true, and both neighbors being candidate points
to be checked in the `run-water` loop. If one or both directions are not resting, then the entire row, including the
current cell, cannot be resting (at least not yet). So the function returns `rests?` mapped to `false`, and the
candidates to examine are whichever neighbors are currently sand; if a neighbor is already running water, there's no
reason to re-examine it.

```clojure
(defn neighbor-cells [point board]
  (let [neighbors [(move-left point) (move-right point)]]
    (letfn [(direction-rests? [p moving-fun]
              (cond
                (= :clay (point-at p board)) true
                (stable? (point-at (move-down p) board)) (recur (moving-fun p) moving-fun)
                :else false))]
      (let [rests? (every? #(direction-rests? (% point) %) [move-left move-right])]
        (if rests?
          {:rests? true :candidates neighbors}
          {:rests? false :candidates (filter #(= :sand (point-at % board)) neighbors)})))))
```

We're almost done... It looks like we just need to parse the input, run the water, and collect the cells that are
water. But this is the part that trapped me for days despite a stream of working test cases! The instructions state
"To prevent counting forever, <u>ignore tiles with a y coordinate smaller than the smallest y coordinate in your
scan data</u> or larger than the largest one." Sigh. We had to set the original bounding box to allow `y` values going
all the way up to the water source; I had test cases that proved we needed that. But now we need to strip out any
water values above the highest clay value, which is... the minimum `y` value from a real bounding box? Rather than
butchering the existing code, we'll just calculate it again.

Given the board (initial or after running the water), use the `keep` function to extract out the `y` value for all
points that are clay, and look search for the minimum.  Note the awesome structured destructuring of the map entries,
which are of type `[[k v] t]`.

```clojure
(defn smallest-y-coord [board]
  (->> board
       (keep (fn [[[_ y] t]] (when (= :clay t) y)))
       (apply min)))
```

Now to solve the problem, we just put the pieces together. First, parse the board and calculate the minimum `y` value
we'll need later. The run the program using `run-water`, filter for cells that are in range of that annoying `y` value\
and which are water (resting or flowing), and count up the cells.

```clojure
(defn part1 [input]
  (let [board (parse-board input)
        min-y (smallest-y-coord board)]
    (->> (run-water board)
         (filter (fn [[[_ y] t]] (and (<= min-y y)
                                      (water? t))))
         count)))
``` 

---

# Part 2

Now we need to run the same program, but only return the resting water, not the flowing water. With a tiny refactoring,
we can get some lovely reuse.

The only difference between part 1 and part 2 is that part 1 wants two types of water cells and part 2 only wants one.
So we'll refactor the `part1` function into the `solve` function, which takes in the input string and the filter
function to run on the type of the cells.  Then we feed the `water?` function into the second argument for part 1, and
a partial function that looks for resting water for part 2. And we're done!

```clojure
(defn solve [input check]
  (let [board (parse-board input)
        min-y (smallest-y-coord board)]
    (->> (run-water board)
         (filter (fn [[[_ y] t]] (and (<= min-y y)
                                      (check t))))
         count)))

(defn part1 [input] (solve input water?))
(defn part2 [input] (solve input (partial = :resting-water)))
```