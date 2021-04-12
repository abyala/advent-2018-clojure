(ns advent-2018-clojure.day09-clist
  (:refer-clojure :exclude [remove])
  (:require [advent-2018-clojure.circular-list :as clist]))

;; This is a rewrite leveraging my circular-list (c-list) component for managing the
;; internals of the board itself.

(defn init-game [num-players]
  {:board           (-> clist/empty-list (clist/insert 0))
   :score           (apply assoc {} (interleave (range num-players) (repeat 0)))
   :turns-completed 0})

(defn next-turn-num [game] (-> game :turns-completed inc))
(defn num-players [game] (-> game :score count))
(defn pick-next-player [game] (-> game next-turn-num (mod (num-players game))))

(defn walk-board [game offset]
  (clist/get (:board game) offset))

(defn insert-after [game offset value]
  (update game :board #(clist/insert % value offset)))

(defn remove [game offset]
  (update game :board #(clist/remove % offset)))

(defn move-normal [game]
  (let [turn# (next-turn-num game)]
    (-> game
        (insert-after 2 turn#)
        (update :board #(clist/rotate-first % (partial = turn#))))))

(defn move-special [game]
  (let [next-marble (next-turn-num game)
        removing-marble (walk-board game -7)]
    (-> game
        (remove -7)
        (update :board #(clist/rotate % -6))
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