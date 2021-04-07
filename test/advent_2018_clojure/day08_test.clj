(ns advent-2018-clojure.day08-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day08 :refer :all]))

(def TEST_DATA "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day08_data.txt"))

(deftest part1-test
  (is (= 138 (part1 TEST_DATA)))
  (is (= 47464 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 66 (part2 TEST_DATA)))
  (is (= 23054 (part2 PUZZLE_DATA))))