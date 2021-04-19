(ns advent-2018-clojure.day06
  (:require [clojure.string :as str]
            [advent-2018-clojure.point :as point]))

(defn parse-point [s]
  (let [[_ x y] (re-matches #"(\d+), (\d+)$" s)]
    [(Integer/parseInt x) (Integer/parseInt y)]))

(defn parse-input [text]
  (->> (str/split-lines text) (map parse-point)))

(defn perimeter-points [[x1 y1] [x2 y2]]
  (let [outer-ys (range y1 (inc y2))
        inner-xs (range (inc x1) x2)]
    (concat (mapv #(vector x1 %) outer-ys)
            (mapv #(vector x2 %) outer-ys)
            (mapv #(vector % y1) inner-xs)
            (mapv #(vector % y2) inner-xs))))

(defn closest [target points]
  (let [options (->> points
                     (map #(hash-map :point % :distance (point/distance target %)))
                     (group-by :distance)
                     (sort-by first)
                     (map second)
                     first)]
    (when (= (count options) 1)
      (-> options first :point))))

(defn part1 [input]
  (let [points (parse-input input)
        [corner1 corner2] (point/bounding-box points)
        perimeter (perimeter-points corner1 corner2)
        all-closest (keep #(closest % points)
                          (point/all-points corner1 corner2))
        infinites (->> perimeter
                       (keep #(closest % points))
                       set)]
    (as-> (frequencies all-closest) x
          (apply dissoc x infinites)
          (sort-by second > x)
          (first x)
          (second x))))

(defn sum-of-distances [target points]
  (->> (map #(point/distance target %) points)
       (apply +)))

(defn part2 [input sum-below]
  (let [points (parse-input input)]
    (->> (point/bounding-box-points points)
         (map #(sum-of-distances % points))
         (filter #(< % sum-below))
         (count))))