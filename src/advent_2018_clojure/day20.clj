(ns advent-2018-clojure.day20
  (:require [clojure.set :as set]
            [advent-2018-clojure.point :as point]))

(def origin [0 0])
(def move-by-dir {\N point/move-north
                  \S point/move-south
                  \E point/move-east
                  \W point/move-west})
(def all-directions (keys move-by-dir))

(defn two-steps [pos dir]
  (let [[_ a b] (iterate (move-by-dir dir) pos)]
    [a b]))

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

(defn move-twice-if-allowed [board pos dir]
  (let [steps (two-steps pos dir)]
    (when (= [:door :room] (map board steps))
      (second steps))))

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

(defn part1 [input]
  (->> (parse-board input)
       (distances-to-origin)
       (map second)
       (apply max)))

(defn part2 [input]
  (->> (parse-board input)
       (distances-to-origin)
       (filter #(>= (second %) 1000))
       count))