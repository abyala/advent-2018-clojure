(ns advent-2018-clojure.day11
  (:require [clojure.string :as str]
            [advent-2018-clojure.summed-area-table :as sat]))

(def max-length 300)

(defn power-level [x y serial-number]
  (let [rack-id (+ x 10)]
    (-> (* rack-id y)
        (+ serial-number)
        (* rack-id)
        (mod 1000)
        (quot 100)
        (- 5))))

(defn create-table [serial-number]
  (sat/create max-length #(power-level %1 %2 serial-number)))

(defn coords-of-max-square [sat min max]
  (->> (for [x (range (inc max-length))
             y (range (inc max-length))
             len (range min (inc max))
             :let [sum (sat/sum-of-square sat x y len)]
             :when (some? sum)]
         [[x y len] sum])
       (sort-by second >)
       ffirst))

(defn part1 [serial-number]
  (-> (create-table serial-number)
      (coords-of-max-square 3 3)
      butlast
      vec))

(defn part2 [serial-number]
  (-> (create-table serial-number)
      (coords-of-max-square 1 max-length)))
