(ns advent-2018-clojure.day21-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day21 :refer :all]))

(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day21_data.txt"))

(deftest part1-test
  (is (= 15823996 (part1 PUZZLE_DATA))))