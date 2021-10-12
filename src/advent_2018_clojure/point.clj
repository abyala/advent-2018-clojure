(ns advent-2018-clojure.point
  (:require [clojure.string :as str]))

(def origin [0 0])
(defn distance [[x1 y1] [x2 y2]]
  (+ (Math/abs ^long (- x1 x2))
     (Math/abs ^long (- y1 y2))))

(defn bounding-box [points]
  (letfn [(min-max [nums] ((juxt (partial apply min) (partial apply max)) nums))]
    (let [[x-min x-max] (->> points (map first) (min-max))
          [y-min y-max] (->> points (map second) (min-max))]
      [[x-min y-min] [x-max y-max]])))

(defn all-points [[x1 y1] [x2 y2]]
  (for [y (range y1 (inc y2))
        x (range x1 (inc x2))]
    [x y]))

(defn bounding-box-points [points]
  (->> points bounding-box (apply all-points)))

(def point-sort (juxt second first))

(defn move-north [[x y]] [x (inc y)])
(defn move-south [[x y]] [x (dec y)])
(defn move-east [[x y]] [(inc x) y])
(defn move-west [[x y]] [(dec x) y])
(def move-up move-south)
(def move-down move-north)

(defn adjacent-points [point]
  ((juxt move-north move-west move-east move-south) point))

(defn surrounding-points [[x y]]
  (for [y' (map #(+ y %) [-1 0 1])
        x' (map #(+ x %) [-1 0 1])
        :when (or (not= x x') (not= y y'))]
    [x' y']))

(defn print-grid [board drawing-fn]
  (let [lines (->> (group-by (comp second first) board)
                   (sort-by first)
                   (map second))
        line-strings (map (fn [line] (->> (sort-by ffirst line)
                                          (map (comp drawing-fn second))
                                          (apply str)))
                          lines)]
    (println (str/join "\n" line-strings))))