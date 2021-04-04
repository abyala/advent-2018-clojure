(ns advent-2018-clojure.day03
  (:require [clojure.string :as str]))

(defn parse-claim [line]
  (let [[_ id x y width height] (re-matches #"#(\d+) @ (\d+),(\d+): (\d+)x(\d+)" line)]
    {:id     id,
     :x      (Integer/parseInt x),
     :y      (Integer/parseInt y),
     :width  (Integer/parseInt width),
     :height (Integer/parseInt height)}))

(defn parse-input [data]
  (->> (str/split-lines data)
       (map parse-claim)))

(defn x-coords [{:keys [:x :width]}]
  (map + (repeat x) (range width)))
(defn y-coords [{:keys [:y :height]}]
  (map + (repeat y) (range height)))

(defn claim-points [claim]
  (for [x (x-coords claim)
        y (y-coords claim)]
    [x y]))

(defn overlaps [claims]
  (->> (map claim-points claims)
       (apply concat)
       (frequencies)
       (keep (fn [[point n]] (when (> n 1) point)))))

(defn part1 [input]
  (-> input parse-input overlaps count))

(defn part2 [input]
  (let [claims (parse-input input)
        overlaps (set (overlaps claims))]
    (->> claims
         (keep (fn [c] (when (not-any? #(overlaps %)
                                   (claim-points c))
                     (:id c))))
         first)))