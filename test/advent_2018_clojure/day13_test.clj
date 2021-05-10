(ns advent-2018-clojure.day13-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day13 :refer :all]))

(def TEST_DATA_1 (slurp "resources/advent_2018_clojure/day13_test_data1.txt"))
(def TEST_DATA_2 (slurp "resources/advent_2018_clojure/day13_test_data2.txt"))
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day13_data.txt"))

(deftest part1-test
  (is (= [7 3] (part1 TEST_DATA_1)))
  (is (= [16 45] (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= [6 4] (part2 TEST_DATA_2)) )
  (is (= [21 91] (part2 PUZZLE_DATA))))