# Day 20: A Regular Map

* [Problem statement](https://adventofcode.com/2018/day/20)
* [Solution code](../src/advent_2018_clojure/day20.clj)

---

This was another fun problem, with a little experimentation with some Clojure functions I don't regularly use. I
thought the solution was a fun use of some new tools... new for me.

I decided to solve this problem in two distinct steps, even though that meant a little double computation. Given a set
of walking instructions through a maze of undefined shape and size, I chose to explore the entire map first, then
determine the distances from the origin to each room in the map, and then solve the actual problem. Fun fact - when
coding in Clojure, it behooves you to find synonyms for the word `map`, like `maze` or in this case `board`, to avoid
unfortunate naming collisions!

---

## Part 1

As always, we start with parsing the input. Note that I originally converted each character into its keyword 
representation (`:north` for `N`) because that's generally a good approach, but it felt like overkill in this problem.
So, uncharacteristic for me, I just stuck with the characters. 

### Defining the board

First, let's create a few helper definitions. I decided that since the actual direction of the board doesn't matter,
I defined the starting point as the `origin` at `[0 0]`, and to treat the cardinal directions as though they sat on a
normal map. Often in Advent Problems, `[0 0]` is at the _top_ of the graph, and the y-axis increases as one moves
_down_ the graph. I started going that way but it doesn't line up well with cardinal directions, so North _increases_
the y-value, as normal people would expect. These functions now reside in the `point` namespace, where `move-by-dir`
associates the `N`, `S`, `E`, and `W` values from the input to these functions. Finally, `all-directions` is a
convenience function to get each of those directional letters.

```clojure
(def origin [0 0])
(def move-by-dir {\N point/move-north
                  \S point/move-south
                  \E point/move-east
                  \W point/move-west})
(def all-directions (keys move-by-dir))
```

To update existing code within the `point` namespace, I modified the `adjacent-points` function to use these new
helper functions. Note again the wonderful `juxt` function to compose each of the `move-*` functions and generate a
vector of applying each of them to the incoming point.

```clojure
; From the advent-2018-clojure.point namespace
(defn move-north [[x y]] [x (inc y)])
(defn move-south [[x y]] [x (dec y)])
(defn move-east [[x y]] [(inc x) y])
(defn move-west [[x y]] [(dec x) y])

(defn adjacent-points [point]
  ((juxt move-north move-west move-east move-south) point))
```

Now because it always takes to steps to go from room to room -- one through a door and another into a room, I created
the helper function `two-steps`. This takes in the current position `[x y]` coordinates and the direction character to
move, and it returns the next two steps in that direction. I like this use of Clojure's deconstructor - we call
`iterate` to get an infinite sequence of moving in one direction from a starting position, and then after discarding
the first value (the original position), we grab the next two and stick them into a vector.  Nice and clean.

```clojure
(defn two-steps [pos dir]
  (let [[_ a b] (iterate (move-by-dir dir) pos)]
    [a b]))

; This is equivalent to
(defn two-steps [pos dir]
  (->> (iterate (move-by-dir dir) pos) rest (take 2)))
```

It's time to define `parse-board`, which used to be several smaller functions but which I combined into one. The idea
is to `reduce` over each character in the input, excluding the starting `^` and trailing `$` characters with the
`remove` function. The reduction data type is a map of the `:board` that associates each point with either a `:door`
or a `:room`; the "current" position on the board, starting with the `origin`; and any waypoints we drop. Whenever the
input opens a parenthesis, we effectively drop a breadcrumb. If we get to a pipe symbol, we move back to the breadcrumb
so the input can take us in another direction. If we get to a close parenthesis, we drop that breadcrumb from the list
of waypoints, since that `(...)` expression is over. This entire map I call an `explorer`, since the board itself
doesn't care if or how someone walks through it, and at this stage, the algorithm is just looking around.

Once we've initialized the map, the function is quite simple. To work with the waypoint, we `conj` the current position
onto the list (create waypoint), drop the first value with `rest` (finish waypoint), or `assoc` the current position in
in the map with with the `first` or head of the waypoints (return to waypoint). If it's none of those values, then it's
a direction. So we take `two-steps` forward, `assoc` the current position of the explorer at the second location, and
then `assoc` into the `:boards` mapping that the neighbor location is a door and the destination is a room.  I
particularly like how that last expression looks: `(update explorer :board assoc neighbor :door destination :room)`.
It reads beautifully to me, since the `update` function can take a variable number of arguments to represent its update
function. So in this case, we are updating the map `explorer` at the field `:board`, but associating onto it the key
pairs of `{neighbor :door, destination :room}`. You can't say Clojure has too many parentheses when looking at this
beauty!

Anyway, after running through the full input, we only want to return the board and not reveal any information about
the explorer, so we dump the `:pos` and `:waypoints` and instead just rip out the `:board` mapping.

```clojure
(defn parse-board [input]
  (-> (reduce (fn [{:keys [waypoints pos] :as explorer} dir]
                (case dir
                  \( (update explorer :waypoints conj pos)
                  \) (update explorer :waypoints rest)
                  \| (assoc explorer :pos (first waypoints))
                  (let [[neighbor destination] (two-steps pos dir)]
                    (-> explorer
                        (assoc :pos destination)
                        (update :board assoc neighbor :door destination :room)))))
              {:board {origin :room} :pos origin :waypoints ()}
              (remove #{\^ \$} input))
      :board))
```

We have a workable board, represented as a simple map of `{pos [:door|:room]}`.  Now let's use it!

### Finding the distances from the origin

Well we're about to walk through the map again from the origin, so here's the duplicate work. I still like this idea
because it reduces the cognitive load by doing one thing at a time. So the goal is to start from the origin, see if we
can take two steps (first through a door and second into a room). If so, then any room we go into which we haven't seen
yet is of distance one greater than the distance of the point being inspected. There are lots of little goodies in here,
so let's go through it.

First, I made a function called `move-twice-if-allowed`, which will return the resulting position after moving in the
same direction twice, but only if it goes through a door and into a room. The `two-steps` function from above gives us
those two positions, but we _don't_ destructure it here because of a nifty finding. Instead of an `and` statement, I
instead mapped each of those two steps to the `board` itself, and since a Clojure map is itself a function, the
expression `(map board steps)` will return the sequence of what the board contains at each step. We compare that
sequence to `[:door :room]` to see if the board allows that movement, and if so, we return the second `step`, which is
the room.

```clojure
(defn move-twice-if-allowed [board pos dir]
  (let [steps (two-steps pos dir)]
    (when (= [:door :room] (map board steps))
      (second steps))))

; It's equivalent to this, which is less idiomatic and harder to read
(defn move-twice-if-allowed [board pos dir]
  (let [[step1 step2] (two-steps pos dir)]
    (when (and (= :door (board step1))
               (= :room (board step2)))
      step2)))
```  

The goal is to build out a `distances-to-origin` function, that maps each position on the map to the number of doors we
had to open to get there. First, let's look at the overall structure of the function. It's going to loop over two 
bindings - `unseen` is a set of all reachable points we haven't walked through yet, and `distances-to` maps each point
to the shortest distance to it. In each iteration, we're going to find the closest unseen point by checking its
distance in `distances-to-second`, aribitrarily picking the first one we find in the case of a match. If there are no
more `unseen` values, then we return the map `distances-to`.

```clojure
(defn distances-to-origin [board]
  (loop [unseen #{origin}, distances-to {origin 0}]
    (if-let [pos (->> unseen (sort-by (comp distances-to second)) first)]
      ; TO BE COMPLETED
      distances-to)))
```  

Now to fill in the missing piece, once we have a position to inspect, we want to find all adjacent points that are both
reachable and which haven't been seen yet. This isn't too bad. We start with the sequence of `all-directions` so we can
look in four directions. Then we'll use the `keep` function to call `move-twice-if-allowed` from the current position
in each of the directions, discarding the `nil` values for invalid moves. Then we call `(remove distances-to points)`
to drop any points we've already visited, and finally we turn the sequence of valid points into a set. Each of these
points will have a distance one greater than the `distance-to` the current point, so we bind that to `new-distance`.

To recurse through the loop, we make two simple sets of changes. First, from the set of `unseen` points, we remove the
current point using the `disj` function, and call `set/union` to combine that set with the set of new target points.
We probably don't need a set per se, but it's a reasonable safety precaution since we never want to inspect the same
point twice. To the map of `distances-to`, we want to merge in a mapping of each of the `target-point`s to the
`new-distance`.  `zipmap` and `repeat` work together beautifully, combining each of the `target-point` values to an
infinite sequence of the value `new-distance`.

All together, the function looks like this:

 ```clojure
 (defn distances-to-origin [board]
   (loop [unseen #{origin}, distances-to {origin 0}]
     (if-let [pos (->> unseen (sort-by (comp distances-to second)) first)]
       (let [target-points (->> all-directions
                                (keep #(move-twice-if-allowed board pos %))
                                (remove distances-to)
                                set)
             new-distance (inc (distances-to pos))]
         (recur (-> unseen (disj pos) (set/union target-points))
                (merge distances-to (zipmap target-points (repeat new-distance)))))
       distances-to)))
```

### Solving the problem

Well now it's easy. Given the input string, we parse it into the board, and find the distances from each point on the
board to the origin. We only care about the distances, so we throw pull out the second value out of the map, and look
for the largest value. Lovely!

```clojure
(defn part1 [input]
  (->> (parse-board input)
       (distances-to-origin)
       (map second)
       (apply max)))
``` 

---

## Part 2

No need to be fancy here; we just need to find the number of room at least 1000 doors away. So as before, we get to the
map of points to their distances from the origin, filter the results by values whose distance are `>=` 1000, and count
them up.

```clojure
(defn part2 [input]
  (->> (parse-board input)
       (distances-to-origin)
       (filter #(>= (second %) 1000))
       count))
```