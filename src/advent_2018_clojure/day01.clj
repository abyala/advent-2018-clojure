(ns advent-2018-clojure.day01
  (:require [clojure.string :as str]))

(defn parse-input [s]
  (->> (str/split-lines s)
       (map #(Integer/parseInt %))))

(defn part1 [s]
  (reduce + 0 (parse-input s)))

(defn part2 [s]
  (reduce (fn [[curr seen] change]
            (let [n (+ curr change)]
              (if (seen n)
                (reduced n)
                [n (conj seen n)])))
          [0 #{}]
          (-> s parse-input cycle)))