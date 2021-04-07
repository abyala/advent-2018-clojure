(ns advent-2018-clojure.day08
  (:require [clojure.string :as str]))

(declare parse-children)

(def header-length 2)

(defn parse-node [nums]
  (let [[n-children meta-length & others] nums
        [children-length children] (parse-children n-children (drop header-length nums))
        metadata (->> others (drop children-length) (take meta-length))
        total-length (+ header-length children-length meta-length)]
    [total-length {:children children :metadata metadata}]))

(defn parse-children [num-children nums]
  (loop [children [], offset 0]
    (if (= num-children (count children))
      [offset children]
      (let [[len child] (parse-node (drop offset nums))]
        (recur (conj children child)
               (+ offset len))))))

(defn parse-tree [input]
  (->> (str/split input #" ")
       (map #(Integer/parseInt %))
       (parse-node)
       second))

(defn deep-sum-of-metadata [{:keys [children metadata]}]
  (+ (apply + metadata)
     (apply + (map deep-sum-of-metadata children))))

(defn deep-node-value [{:keys [children metadata]}]
  (if (empty? children)
    (apply + metadata)
    (->> metadata
         (map #(-> (get children (dec %) {}) deep-node-value))
         (apply +))))

(defn part1 [input] (-> input parse-tree deep-sum-of-metadata))
(defn part2 [input] (-> input parse-tree deep-node-value))