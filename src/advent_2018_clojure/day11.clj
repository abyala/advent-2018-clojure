(ns advent-2018-clojure.day11
  (:require [clojure.string :as str]))

(def max-length 300)
(defn rack-id [x] (+ x 10))

(defn power-level [[x y] serial-number]
  (-> (* (rack-id x) y)
      (+ serial-number)
      (* (rack-id x))
      (mod 1000)
      (quot 100)
      (- 5)))

(def all-coords
  (for [y (range 1 (inc max-length))
        x (range 1 (inc max-length))]
    [x y]))

(defn power-levels [serial-number]
  (->> all-coords
       (map #(hash-map % (power-level % serial-number)))
       (apply merge)))

(defn total-power [[x y] levels]
  (->> (for [px (range x (+ x 3))
             py (range y (+ y 3))]
         (levels [px py]))
       (apply +)))

(defn total-power2 [[x y] len levels]
  (->> (for [px (range x (+ x len))
             py (range y (+ y len))]
         (levels [px py]))
       (apply +)))

(defn part1 [serial-number]
  (let [levels (power-levels serial-number)]
    (->> (for [y (range 1 (dec max-length))
               x (range 1 (dec max-length))]
           [[x y] (total-power [x y] levels)])
         (apply max-key second)
         first)))

(defn part2 [serial-number]
  (let [levels (power-levels serial-number)]
    (->> (for [len (range 1 (inc max-length))
               :let [upper (- (inc max-length) len)]
               y (range 1 (inc upper))
               x (range 1 (inc upper))]
           [x y len])
         (map (fn [[x y len]] [(str/join "," [x y len])
                               (total-power2 [x y] len levels)] ))
         (apply max-key second)
         first)))


;; THIS IS TOO SLOW.
;; Consider taking each square, calculating its square at size 1,
;; Then adding the next marginal perimeter, until reaching x=300 or y=11.
;; Then go back around again.

;; TRY THIS: Map each cell to the sum of ranges