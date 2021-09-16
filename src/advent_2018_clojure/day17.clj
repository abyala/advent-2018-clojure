(ns advent-2018-clojure.day17
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [advent-2018-clojure.point :as point]))

(def water-spring [500 0])

(defn parse-coords [line]
  (let [[_ fixed-axis fixed-val min-val max-val] (re-find #"([x|y])=(\d+), [x|y]=(\d+)\.\.(\d+)" line)
        fixed-range (repeat (Integer/parseInt fixed-val))
        moving-range (range (Integer/parseInt min-val) (inc (Integer/parseInt max-val)))]
    (case fixed-axis "x" (map vector fixed-range moving-range)
                     "y" (map vector moving-range fixed-range))))

(defn parse-input [input]
  (->> input str/split-lines (mapcat parse-coords)))

(defn bounded-box [points]
  (let [min-max (juxt (partial apply min) (partial apply max))
        [min-x max-x] (min-max (map first points))
        [_     max-y] (min-max (map second points))]
    {:min-x (dec min-x),
     :max-x (inc max-x),
     :min-y 0                                               ; Min-y must always be 0 to include the spring
     :max-y max-y}))

(defn parse-board [input]
  (let [clay-points (set (parse-input input))
        {:keys [min-x max-x min-y max-y]} (bounded-box clay-points)]
    (into {} (concat (map vector (point/all-points [min-x min-y] [max-x max-y]) (repeat :sand))
                     (map vector clay-points (repeat :clay))))))

(defn point-at [point board]
  (board point))

(defn move-left [[x y]]  [(dec x) y])
(defn move-right [[x y]] [(inc x) y])
(defn move-down [[x y]]  [x (inc y)])

(defn water? [t] (#{:flowing-water :resting-water} t))
(defn supports-resting-water? [t] (#{:resting-water :clay} t))

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

(defn neighbor-cells [point board]
  (let [neighbors [(move-left point) (move-right point)]]
    (letfn [(direction-rests? [p moving-fn]
              (cond
                (= :clay (point-at p board)) true
                (supports-resting-water? (point-at (move-down p) board)) (recur (moving-fn p) moving-fn)
                :else false))]
      (let [rests? (every? #(direction-rests? (% point) %) [move-left move-right])]
        (if rests?
          {:rests? true :candidates neighbors}
          {:rests? false :candidates (filter #(= :sand (point-at % board)) neighbors)})))))

(defn run-water [board]
  (loop [drips-to-check (list (move-down water-spring)) current-board board]
    (if-let [drip (first drips-to-check)]
      (let [below (move-down drip)
            type-below (point-at below current-board)]
        (cond
          ; If this is clay or already resting water, don't overthink it.
          (#{:resting-water :clay} (point-at drip current-board)) (recur (rest drips-to-check) current-board)

          ; If there's sand below, we don't know anything. We may be flowing water now, but later could become
          ; resting water. Best to not make any decision yet and come back later after the lower cells resolve.
          (= :sand type-below) (recur (cons below drips-to-check) current-board)

          ; If the point below is flowing water, all this cell can be is flowing water
          (= :flowing-water type-below) (recur (rest drips-to-check) (assoc current-board drip :flowing-water))

          ; If the point below is "solid" (clay or resting), then check the neighbors
          (supports-resting-water? type-below) (let [{:keys [rests? candidates]} (neighbor-cells drip current-board)]
                                                 (recur (apply conj (rest drips-to-check) candidates)
                                                        (assoc current-board drip (if rests? :resting-water :flowing-water))))
          ; Only the abyss below us
          :else (recur (rest drips-to-check) (assoc current-board drip :flowing-water)) ))
      current-board)))

(defn smallest-y-coord [board]
  (->> board
       (keep (fn [[[_ y] t]] (when (= :clay t) y)))
       (apply min)))

(defn solve [input check]
  (let [board (parse-board input)
        min-y (smallest-y-coord board)]
    (->> (run-water board)
         (filter (fn [[[_ y] t]] (and (<= min-y y)
                                      (check t))))
         count)))

(defn part1 [input] (solve input water?))
(defn part2 [input] (solve input (partial = :resting-water)))