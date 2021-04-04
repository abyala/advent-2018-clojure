(ns advent-2018-clojure.day03-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day03 :refer :all]
            [clojure.string :as str]))

(def TEST_DATA "#1 @ 1,3: 4x4\n#2 @ 3,1: 4x4\n#3 @ 5,5: 2x2")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day03_data.txt"))

(deftest part1-test
  (is (= 4 (part1 TEST_DATA)))
  (is (= 110389 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= "3" (part2 TEST_DATA)))
  (is (= "552" (part2 PUZZLE_DATA))))