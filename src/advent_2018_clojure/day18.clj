(ns advent-2018-clojure.day18
  (:require [clojure.set :as set]
            [advent-2018-clojure.point :as point]
            [advent-2018-clojure.utils :as utils]))

(def map-characters {\. :open, \| :tree, \# :lumberyard})
(def no-cells-of-each-type {:open 0, :tree 0, :lumberyard 0})

(defn print-board [board] (point/print-grid board (set/map-invert map-characters)))

(defn parse-input [input]
  (-> input
      (utils/parse-to-char-coords)
      (utils/update-values map-characters)))

(defn cell-at [board coords] (board coords))

(defn neighbors [board coords]
  (merge no-cells-of-each-type
         (->> (point/surrounding-points coords)
              (keep #(cell-at board %))
              frequencies)))

(defn cell-next-turn [board coords]
  (let [n (neighbors board coords)]
    (case (cell-at board coords)
      :open (if (>= (:tree n) 3) :tree :open)
      :tree (if (>= (:lumberyard n) 3) :lumberyard :tree)
      :lumberyard (if (and (>= (:lumberyard n) 1)
                           (>= (:tree n) 1))
                    :lumberyard :open))))

(defn next-turn [board]
  (reduce-kv (fn [next-board coords _] (assoc next-board coords (cell-next-turn board coords)))
             {}
             board))

(defn resource-value [board]
  (let [freqs (merge no-cells-of-each-type (-> board vals frequencies))]
    (* (:tree freqs) (:lumberyard freqs))))

(defn part1 [input]
  (-> (iterate next-turn (parse-input input))
      (nth 10)
      (resource-value)))

(defn find-board-loop [initial-board]
  (reduce (fn [{:keys [boards seen]} generation]
            (let [next-board (next-turn (boards generation))]
              (if-let [original-index (seen next-board)]
                (reduced {:boards boards, :loop-idx original-index})
                {:boards (conj boards next-board), :seen (assoc seen next-board (inc generation))})))
          {:boards [initial-board], :seen {initial-board 0}}
          (range)))

(defn board-at-index [boards loop-idx target]
  (if (< target (count boards))
    (boards target)
    (let [loop-size (- (count boards) loop-idx)]
      (boards (+ loop-idx
                 (mod (- target loop-idx) loop-size))))))

(defn part2 [input]
  (let [{:keys [boards loop-idx]} (find-board-loop (parse-input input))
        target-board (board-at-index boards loop-idx 1000000000)]
    (resource-value target-board)))
