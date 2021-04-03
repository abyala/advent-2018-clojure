(ns advent-2018-clojure.day02
  (:require [clojure.string :as str]))

(defn any-letter-n-times? [s n]
  (->> (frequencies s)
       (map second)
       (filter #(= n %))
       (seq)))

(defn num-words-with-n-letters [words n]
  (->> words
       (filter #(any-letter-n-times? % n))
       count))

(defn part1 [words]
  (* (num-words-with-n-letters words 2)
     (num-words-with-n-letters words 3)))

(defn word-pairs [words]
  (let [sorted-words (sort words)]
    (for [w1 sorted-words
          w2 sorted-words :while (pos? (compare w1 w2))]
      [w2 w1])))

(defn same-letters [word1 word2]
  (->> (interleave word1 word2)
       (partition 2)
       (keep (fn [[c1 c2]] (when (= c1 c2) c1)))
       (apply str)))

(defn part2 [words]
  (let [target-length (-> words first count dec)]
    (->> (word-pairs words)
         (map #(same-letters (first %) (second %)))
         (filter #(= target-length (count %)))
         first)))