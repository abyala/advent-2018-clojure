(ns advent-2018-clojure.day10
  (:require [clojure.string :as str]
            [advent-2018-clojure.point :as point]))

(defn parse-stars [input]
  (->> (str/split-lines input)
       (map #(let [[_ px py vx vy] (re-matches #"[^-\d]+([-\d]+)[^-\d]+([-\d]+)[^-\d]+([-\d]+)[^-\d]+([-\d]+)>" %)]
               {:position [(Integer/parseInt px) (Integer/parseInt py)]
                :velocity [(Integer/parseInt vx) (Integer/parseInt vy)]}))))

(defn move-star [{:keys [position velocity] :as star}]
  (assoc star :position (mapv + position velocity)))

(defn move-stars [stars]
  (map move-star stars))

(defn star-bounding-box-size [stars]
  (->> stars (map :position) point/bounding-box (apply point/distance)))

(defn starmap [stars]
  (let [positions (set (map :position stars))]
    (->> (point/bounding-box-points positions)
         (group-by second)
         (sort-by first)
         (map (fn [[_ p]] (->> p
                               (map #(if (positions %) \# \space))
                               (apply str)))))))

(defn print-starmap [stars]
  (dorun (map #(println %) (starmap stars))))

(defn star-message [input]
  (->> (parse-stars input)
       (iterate move-stars)
       (partition 2 1)
       (keep-indexed (fn [idx [a b]] (when (< (star-bounding-box-size a)
                                              (star-bounding-box-size b))
                                       [idx a])))
       first))

(defn part1 [input]
  (-> input star-message second print-starmap))

(defn part2 [input]
  (-> input star-message first))
