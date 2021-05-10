(ns advent-2018-clojure.day13-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day13 :refer :all]))

(def TEST_DATA (slurp "resources/advent_2018_clojure/day13_test_data.txt"))
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day13_data.txt"))

(deftest part1-test
  (is (= [7 3] (time (part1 TEST_DATA))))
  (is (= [16 45] (time (part1 PUZZLE_DATA)))))