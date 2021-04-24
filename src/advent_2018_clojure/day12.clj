(ns advent-2018-clojure.day12
  (:require [clojure.string :as str]))

(def plant-char \#)
(def no-plant-char \.)

(defn plant? [x] (#{plant-char (str plant-char)} x))

(defn parse-input [input]
  (let [[first-line _ & other-lines] (str/split-lines input)]
    {:initial-plants (->> (subs first-line 15)
                          (keep-indexed #(when (plant? %2) %1))
                          set)
     :plant-rules    (->> other-lines
                          (map #((comp vec rest) (re-matches #"(.+) => (.)" %)))
                          (keep #(when (plant? (second %)) (first %)))
                          set)}))

(defn next-turn [plants rules]
  (->> (for [n (range (- (apply min plants) 2)
                      (+ (apply max plants) 3))
             :let [pattern (->> (range (- n 2) (+ n 3))
                                (mapv #(if (plants %) plant-char no-plant-char))
                                (apply str))]
             :when (rules pattern)]
         n)
       set))

(defn minimized [plants]
  (let [floor (apply min plants)]
    (->> plants (map #(- % floor)) set)))

(defn plant-generation-seq [input]
  (let [{:keys [initial-plants plant-rules]} (parse-input input)]
    (iterate #(next-turn % plant-rules) initial-plants)))

(defn plants-at-generation [input num-generations]
  (loop [n num-generations
         [this-gen next-gen :as generations] (plant-generation-seq input)]
    (cond
      (zero? n) this-gen

      ; Each generation that looks the same may still be offset, so add in the difference for each generation
      (= (minimized this-gen) (minimized next-gen)) (let [diff (- (apply min next-gen)
                                                                  (apply min this-gen))
                                                          gen-diff (* diff n)]
                                                      (set (map #(+ gen-diff %) this-gen)))
      :else (recur (dec n) (rest generations)))))

(defn solve [input num-generations]
  (->> (plants-at-generation input num-generations)
       (apply +)))

(defn part1 [input] (solve input 20))
(defn part2 [input] (solve input 50000000000))