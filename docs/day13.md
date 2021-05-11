# Day 13: Mine Cart Madness

* [Problem statement](https://adventofcode.com/2018/day/13)
* [Solution code](../src/advent_2018_clojure/day13.clj)

---

This problem dealt with several carts travelling around a complex overlapping track, and like a bunch of psychopaths,
gleefully watching them crash from the sidelines. I enjoyed this problem because it messed with my head by combining
ordinal directions (North, South, East, West) with relative turns (Left, Straight, Right).

---

## Part 1

As with all Advent Of Code problems, we start by determining the state of the problem space we want to use. In this
case, I consider a state as having a map of each `[x y]` coordinate to its cell on the track, a map of `[x y]`
coordinates to each cart, and a vector of crashes we've seen thus far.

To help with the parsing, I created utility function called `parse-to-char-coords` in the `utils` namespace. This
takes in a multi-line String, and returns a sequence of two-element vectors of `[[x y] c]` that map the coordinates
to the character at that location. This might have been modeled as a simple `[x y c]` three-tuple, but I figure that
we often want to see coordinates together. So this function does two nested calls to `map-indexed`, and then combines
the results with `apply concat`.

```clojure
(defn parse-to-char-coords [input]
  (->> (str/split-lines input)
       (map-indexed (fn [y line]
                      (map-indexed (fn [x c] [[x y] c]) line)))
       (apply concat)))
``` 

With that out of the way, we start with parsing the input. The String that describes a track has a few
elements: empty spaces, straightaways, turns, intersections, and carts. From a parsing perspective, we can ignore both
empty spaces and straightaways because neither of them affect the carts; carts don't travel through space in this
world, and a cart on a straightaway continues in its previous direction.

I start by defining two functions called `c->path` and `c->dir`, which parse all turns and intersections, and all
carts, respectively. As a general rule, we want to get away from the character strings as quickly as possible,
replacing them with keyword definitions. That said, I couldn't find a nicer way to describe lower-left-upper-right
corners than `:slash` and its opposite being `:backslash`, so I went with it. As usual, we can just throw the
character into a map of character-to-keyword to see if it's a match.

```clojure
(defn c->path [c] ({\\ :backslash \/ :slash \+ :intersection} c))
(defn c->dir  [c] ({\^ :north \v :south \> :east \< :west} c))
```

I also decided to represent a cart as a record instead of a simple map. It wasn't strictly necessary, but a cart
just seemed like a "thing" that wanted to assert its data, so I went for it. It's no biggie, but it does provide
access to the `->Cart` function for future construction. The `next-intersection` element describes what a cart should
do when it comes to an intersection.

```clojure
(defrecord Cart [coords dir next-intersection])
```

Now it's time to parse. Rather than a `loop-recur`, I went with a reducing function over the `parse-to-char-coords`
tuples described earlier. From an empty state, we destructure the tuple into `[coords c]` and use `condp apply` to
determine which parsing function (`c->path` or `c->dir`) can parse the character. Since both `:cells` and `:carts`
in the state are maps of the coordinates to the cell or cart, in both cases we use `(assoc-in state [:cells coords] foo)`
to add the new character into state. As a fall-through for `condp`, we pass through the current version of the state,
as that covers both empty spaces and straightaways. I could have filtered these out up-front, but that seemed
unnecessary.

```clojure
(defn parse-input [input]
  (reduce (fn [state [coords c]] (condp apply [c]
                                   c->path (assoc-in state [:cells coords] (c->path c))
                                   c->dir  (assoc-in state [:carts coords] (->Cart coords (c->dir c) :left))
                                   state))
          {:cells {} :carts {} :crashes []}
          (utils/parse-to-char-coords input)))
```

I added two other helper functions near the top of the file, which handle relative and ordinate directions. For
relative directions, we have instructions on how to handle intersections. Each cart cycles through going left, then
straight, then right for each intersection, so `next-intersection-dir` maps each relative direction to the next
direction in the cycle.

```clojure
(defn next-intersection-dir [dir] ({:left     :straight
                                    :straight :right
                                    :right    :left} dir))
```

For ordinate directions, my solution is hardly the prettiest one, but it simplifies the code. For each of the four
directions, it has mappings for which direction to face if going straight through it, turning left or right, or 
reaching a `slash` or `backslash` turn. Sometimes simplicity is better than prettiness!

```clojure
(def directions
  {:north {:straight :north, :left :west,  :right :east,  :slash :east,  :backslash :west}
   :east  {:straight :east,  :left :north, :right :south, :slash :north, :backslash :south}
   :south {:straight :south, :left :east,  :right :west,  :slash :west,  :backslash :east}
   :west  {:straight :west,  :left :south, :right :north, :slash :south, :backslash :north}})
```

Now it's time to take a cart and ask it to blindly move forward, which means that given a pair of coordinates and an
ordinal direction, return the new coordinates. As we've seen in other AoC years, we can call `(mapv + coords1 coords2)`
to apply the mapping function `+` to the `nth` values of each ordinate vector, and return a new vector. Remember we
want to use `mapv` instead of `map` to return vectors instead of sequences.

```clojure
(defn move-forward [coords dir]
  (mapv + coords (case dir
                   :north [0 -1]
                   :south [0 1]
                   :west [-1 0]
                   :east [1 0])))
```
 
A cart has to move on the track, adjusting its directions at turns and intersections, so the `move-cart` function has 
to take in both the current state and the cart needing to move. We start by calculating the new coordinates by calling 
the above `move-forward` function, and then we check out what cell is at those coordinates. If there's nothing there,
it's presumably a straightaway, so just update the cart's coordinates. If its an intersection, then use the 
`directions` map with the cart's current direction and the `next-intersection` instruction on what to do when it gets 
there; then call `next-intersection-dir` to pick what to do at the _next_ intersection. And if it's neither of those
things, then it's a turn, so again call `directions` with the cell's `slash` or `backslash` identity.

```clojure
(defn move-cart [state {:keys [coords dir next-intersection] :as cart}]
  (let [new-coords (move-forward coords dir)
        cell (get (:cells state) new-coords :straightaway)]
    (case cell
      :straightaway (assoc cart :coords new-coords)
      :intersection (->Cart new-coords
                            (-> directions dir next-intersection)
                            (next-intersection-dir next-intersection))
      (->Cart new-coords (-> directions dir cell) next-intersection))))
```

Our goal is leading up to the `next-turn` function, which will take a current state and move all of the carts. To do
this, we need to determine the order of carts before any of them move. Since the state models carts as a map of their
coordinates to the carts themselves, I made a `coord-sort` function that applies to just the coordinates. We want to
order carts by `y-coord` before `x-coord`, and `juxt` comes to the rescue here. Remember that this funny function
takes in `n` functions and returns a new function that, when applied to its argument, returns a vector of applying
each of its function to the argument. In this case, we just use `(juxt second first)` to grab the second argument of
the coordinates (`y`) before the first argument (`x`).

```clojure
(defn coord-sort [coords]
  (sort-by (juxt first second) coords))
```

That's all well and good, but we were promised exploding carts, and I'm thoroughly disappointed at the lack of
mechanical carnage! The `collides?` function looks at the current state of the world and the coordinates onto which
the cart wants to travel, and it returns the coordinates if some cart already exists there.

```clojure
(defn collides? [state coords]
  (some #(= coords %) (-> state :carts keys)))
```

Ok, that wasn't too dramatic. Let's get to the meat of the problem now: the fabled `next-turn` function! First of all,
I use a `reduce` function again, moving each cart in order over the current state. In this way, we ensure that each
cart runs based on its initial order from `coord-sort`. Here's how I'd read this function.
1. First, ignore the `if-let`. We'll come back to it in a second.
1. Define `cart` to be the new definition of the cart, previously at `coords`, after it moves along the track. Rather
than destructuring it, I just defined `new-coords` to be the coordinates at the new location.
1. If there is a collision at the new coordinates, then we have to remove both carts from the track and add the
crash location to the `:crashes:` vector of the state. I really using the `->` threading macro when doing multiple
mutations to a single map, since the map is the first argument after `update`, `assoc`, `update-in`, and `assoc-in`.
To avoid performing two separate updates to remove each cart, we can call `#(apply dissoc % [coords new-coords])` to
remove both the cart at the old location (the one that's moving) and the cart at the new location (the one getting hit).
If there's no collision, then we just dissociate the cart from its old coordinates and associate it to the new
coordinates.
1. Finally, we go back up to that pesky `if-let`. If a cart crashes into a cart that hasn't moved yet this go-around,
we don't want the charred remains of the cart to move when it's its turn. Since we `dissoc` away the coords of the
cart that exploded, we can skip over the cart if it's no longer there.

 ```clojure
 (defn next-turn [state]
   (reduce (fn [next-state coords]
             (if-let [old-cart ((next-state :carts) coords)] ; Make sure cart hasn't been hit yet
               (let [cart (move-cart next-state old-cart)
                     new-coords (:coords cart)]
                 (if (collides? next-state new-coords)
                   (-> next-state
                       (update :carts #(apply dissoc % [coords new-coords]))
                       (update :crashes conj new-coords))
                   (update next-state :carts #(-> (dissoc % coords)
                                                  (assoc new-coords cart)))))
               next-state))
           state
           (coord-sort (-> state :carts keys))))
```

Alright, time to finish part 1 by putting it all together. All we need to know are the coordinates of the first crash.
So after parsing the input, we iterate on the `next-turn` function, map it to the `:crashes` vector, and filter out
the first vector that's not empty using the `seq` function; remember that this is more idiomatic than calling
`(filter #(not (empty? %)))` according to the docs. Past that filter, we need to call `ffirst` to get the first element
of the first vector of crashes. And that's it!

```clojure
(defn part1 [input]
  (->> (parse-input input)
       (iterate next-turn)
       (map :crashes)
       (filter seq)
       ffirst))
```

---

## Part 2

There's not much more work to be done here, given the information we have within our state. Now instead of waiting
for the first crash, we have to wait until there's only a single cart on the track. Structurally this is the same as
the part 1 solution, but the mapping function now says `(map (comp keys :carts))`; this says to map each state to the
right-to-left composition that grabs the `:carts` map and then grabs the keys, which in this case are the coordinates.
If the filter identifies there is only a single cart left, then again use ``ffirst`` to grab its coordinates.

```clojure
(defn part2 [input]
  (->> (parse-input input)
       (iterate next-turn)
       (map (comp keys :carts))
       (filter #(= 1 (count %)))
       ffirst))
```
