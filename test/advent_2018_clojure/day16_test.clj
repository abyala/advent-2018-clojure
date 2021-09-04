(ns advent-2018-clojure.day16-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day16 :refer :all]))

(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day16_data.txt"))

(deftest part1-test
  (is (= 651 (part1 PUZZLE_DATA))))