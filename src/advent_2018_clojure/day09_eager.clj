(ns advent-2018-clojure.day09_eager)

;; This solution uses a vector to represent its board. It's efficient for finding
;; elements, especially on the "special turn" on the 23s, but all of the eager
;; vector copying is quite inefficient. Part 2 would have taken well over an hour
;; to run to completion.

(def empty-board [0])
(defn init-game [num-players]
  {:board empty-board,
   :score (apply assoc {} (interleave (range num-players) (repeat 0)))
   :next-player 0
   :turns-completed 0})

(defn next-turn-num [game] (-> game :turns-completed inc))

(defn normal-turn [{:keys [board] :as game}]
  (let [marble (next-turn-num game)
        [curr a b & others] board]
    (assoc game :board (cond
                         (nil? a) [marble curr]
                         (nil? b) [marble curr a]
                         :else (vec (concat [marble] [b] others [curr a]))))))

(defn special-turn [{:keys [board next-player] :as game}]
  (let [index (- (count board) 7)]
    (-> game
        (assoc :board (vec (concat (subvec board (inc index))
                                        (subvec board 0 index))))
        (update-in [:score next-player] #(+ %
                                            (board index)
                                            (next-turn-num game))))))

(defn next-turn [{:keys [score next-player] :as game}]
  (let [turn (if (zero? (mod (next-turn-num game) 23))
               (special-turn game)
               (normal-turn game))]
    (-> turn
        (assoc :next-player (-> next-player inc (mod (count score))))
        (update :turns-completed inc))))

(defn high-score [game]
  (->> game :score (map second) (sort >) first))

(defn part1 [num-players last-marble]
  (println "Original")
  (->> (init-game num-players)
       (iterate next-turn)
       ;;(drop last-marble)
       ;;first
       (drop last-marble)
       ;;last
       first
       high-score))