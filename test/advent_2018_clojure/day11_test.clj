(ns advent-2018-clojure.day11-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day11 :refer :all]))

(def PUZZLE_DATA 9435)

(deftest power-level-test
  (are [x y serial power] (= power (power-level x y serial))
                          3 5 8 4
                          122 79 57 -5
                          217 196 39 0
                          101 153 71 4))

(deftest part1-test
  (is (= [33 45] (part1 18)))
  (is (= [21 61] (part1 42)))
  (is (= [20 41] (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= [90 269 16] (part2 18)))
  (is (= [232 251 12] (part2 42)))
  (is (= [236 270 11] (part2 PUZZLE_DATA))))