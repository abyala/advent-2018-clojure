(ns advent-2018-clojure.day14
  (:require [clojure.string :as str]))

(def initial-state {:scores [3 7] :elves  [0 1]})

(defn int-digits [n]
  (mapv #(Integer/parseInt (str %))
        (str n)))

(defn next-turn [{:keys [scores elves] :as state}]
  (let [new-recipes (->> (map #(get scores %) elves)
                         (apply +)
                         (int-digits))
        new-scores (apply conj scores new-recipes)
        new-elves (mapv #(mod (+ % (scores %) 1)
                              (count new-scores))
                        elves)]
    (assoc state :scores new-scores :elves new-elves)))

(def recipe-seq
  (map #(apply str (:scores %))
       (iterate next-turn initial-state)))

(defn part1 [num-recipes]
  (->> (iterate next-turn initial-state)
       (drop-while #(< (count (:scores %))
                       (+ num-recipes 10)))
       first
       :scores
       (drop num-recipes)
       (take 10)
       (apply str)))

(defn part2 [target]
  (->> recipe-seq
       (keep #(str/index-of % target))
       first))