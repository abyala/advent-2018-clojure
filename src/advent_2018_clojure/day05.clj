(ns advent-2018-clojure.day05
  (:require [clojure.string :as str]))

(def all-letter-pairs
  (map #(hash-set (char (+ (int \A) %))
                  (char (+ (int \a) %)))
       (range 26)))

(defn destroys? [^Character c1, ^Character c2]
  (and (not= c1 c2)
       (= (Character/toLowerCase c1) (Character/toLowerCase c2))))

(defn remove-two [word index]
  (str (subs word 0 index)
       (subs word (+ index 2))))

(defn react-polymer [polymer]
  (loop [p polymer, idx 0]
    (if (>= idx (dec (count p)))
      p
      (if (destroys? (get p idx) (get p (inc idx)))
        (recur (remove-two p idx) (max 0 (dec idx)))
        (recur p (inc idx))))))

(defn part1 [polymer]
  (-> polymer react-polymer count))

(def capital-letters
  (map #(char (+ (int \A) %)) (range 26)))

(defn remove-letters-insensitive [word c]
  (let [pattern (re-pattern (str "(?i)" c))]
    (str/replace word pattern "")))

(defn part2 [polymer]
  (->> capital-letters
       (map (comp count
                  react-polymer
                  #(remove-letters-insensitive polymer %)))
       (apply min)))