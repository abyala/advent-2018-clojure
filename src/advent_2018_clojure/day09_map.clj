(ns advent-2018-clojure.day09-map
  (:refer-clojure :exclude [remove]))

;; This is by far the best of the algorithms I came up with.
;; A simple linked list or vector might have been ok, except that this is
;; a doubly linked list whose head keeps changing. For such a structure,
;; a map handles these connections much better than a list or vector.

(defn init-game [num-players]
  {:board           {0 [0 0]}
   :current-marble  0
   :score           (apply assoc {} (interleave (range num-players) (repeat 0)))
   :turns-completed 0})

(defn next-turn-num [game] (-> game :turns-completed inc))
(defn num-players [game] (-> game :score count))
(defn pick-next-player [game] (-> game next-turn-num (mod (num-players game))))
(defn neighbors-of [game marble] (-> game :board (get marble)))
(defn neighbor-right [game marble] (second (neighbors-of game marble)))
(defn neighbor-left [game marble] (first (neighbors-of game marble)))

(defn walk-board [game offset]
  (loop [marble (:current-marble game), n offset]
    (cond
      (zero? n) marble
      (pos? n) (recur (neighbor-right game marble) (dec n))
      :else (recur (neighbor-left game marble) (inc n)))))

(defn insert-after [game after value]
  (let [old-neighbor (neighbor-right game after)]
    (update game :board (fn [b] (-> b
                                    (update after #(assoc % 1 value))
                                    (update old-neighbor #(assoc % 0 value))
                                    (assoc value [after old-neighbor]))))))

(defn remove [game key]
  (let [[left right] (neighbors-of game key)]
    (update game :board (fn [b] (-> b
                                    (update left #(assoc % 1 right))
                                    (update right #(assoc % 0 left))
                                    (dissoc key))))))

(defn move-normal [game]
  (let [marble (next-turn-num game)
        after (walk-board game 1)]
    (-> game
        (insert-after after marble)
        (assoc :current-marble marble))))

(defn move-special [game]
  (let [next-marble (next-turn-num game)
        removing-marble (walk-board game -7)
        next-current (neighbor-right game removing-marble)]
    (-> game
        (remove removing-marble)
        (assoc :current-marble next-current)
        (update-in [:score (pick-next-player game)] + next-marble removing-marble))))

(defn special-turn? [game]
  (-> (next-turn-num game)
      (mod 23)
      zero?))

(defn take-turn [game]
  (-> game
      ((if (special-turn? game) move-special move-normal))
      (update :turns-completed inc)))

(defn high-score [game]
  (->> game :score (map second) (sort >) first))

(defn part1 [num-players last-marble]
  (->> (init-game num-players)
       (iterate take-turn)
       (drop last-marble)
       first
       high-score))

(defn part2 [num-players last-marble]
  (part1 num-players (* last-marble 100)))