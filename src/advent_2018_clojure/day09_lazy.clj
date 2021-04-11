(ns advent-2018-clojure.day09-lazy)

;; This algorithm uses a board with a singly linked list, and all
;; lazy operations. It didn't stand a chance of completing, and took about
;; five times as long as even the slow eager solution did for part 1.

(defn init-game [num-players]
  {:board           (list 0),
   :score           (apply assoc {} (interleave (range num-players) (repeat 0)))
   :next-player     0
   :turns-completed 0})

(defn next-turn-num [game] (-> game :turns-completed inc))
(defn num-players [game] (-> game :score count))
(defn pick-next-player [game] (-> game :next-player inc (mod (num-players game))))

(defn normal-turn
  ([game] (normal-turn game (next-turn-num game)))
  ([game marble]
   (let [board (:board game)
         offset (mod 2 (count board))]
     (assoc game :board (concat (cons marble (drop offset board))
                                (take offset board))))))

  (defn special-turn [game]
    (let [board (:board game)
          next-player (:next-player game)
          pivot (- (count board) 7)]
      (-> game
          (assoc :board (concat (drop (inc pivot) board)
                                (take pivot board)))
          (update-in [:score next-player] #(+ %
                                              (nth board pivot)
                                              (next-turn-num game))))))

(defn next-turn [game]
  (let [turn (if (zero? (mod (next-turn-num game) 23))
               (special-turn game)
               (normal-turn game))]
    (-> turn
        (assoc :next-player (pick-next-player game))
        (update :turns-completed inc))))

(defn high-score [game]
  (->> game :score (map second) (sort >) first))

(defn part1 [num-players last-marble]
  (->> (init-game num-players)
       (iterate next-turn)
       (drop last-marble)
       first
       high-score))