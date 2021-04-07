(ns advent-2018-clojure.day08
  (:require [clojure.string :as str]))

(def header-length 2)
(declare parse-children)

(defn node-value [children metadata]
  (if (empty? children)
    (apply + metadata)
    (->> metadata
         (map #(get-in children [(dec %) :value] 0))
         (apply +))))

(defn parse-node [nums]
  (let [[n-children meta-length & others] nums
        children (parse-children n-children (drop header-length nums))
        children-length (->> (map :length children) (apply +))
        metadata (->> others (drop children-length) (take meta-length))
        total-length (+ header-length children-length meta-length)]
    {:children     children
     :metadata     metadata
     :metadata-sum (+ (apply + metadata)
                      (apply + (map :metadata-sum children)))
     :length       total-length
     :value        (node-value children metadata)}))

(defn parse-children [num-children nums]
  (loop [children [], offset 0]
    (if (= num-children (count children))
      children
      (let [child (parse-node (drop offset nums))]
        (recur (conj children child)
               (+ offset (:length child)))))))

(defn parse-tree [input]
  (parse-node (map #(Integer/parseInt %)
                   (str/split input #" "))))

(defn part1 [input] (-> input parse-tree :metadata-sum))
(defn part2 [input] (-> input parse-tree :value))