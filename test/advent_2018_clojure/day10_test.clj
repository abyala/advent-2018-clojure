(ns advent-2018-clojure.day10-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day10 :refer :all]))

(def TEST_DATA (slurp "test/advent_2018_clojure/day10_test_data.txt"))
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day10_data.txt"))

(deftest parse-stars-test
  (is (= {:position [9 1] :velocity [0 2]}
         (-> TEST_DATA parse-stars first)))
  (is (= {:position [7 0] :velocity [-1 0]}
         (-> TEST_DATA parse-stars second))))

; It's unreasonable to test the printout of part 1, so feel free to
; run these lines to see what it prints out.
; (part1 TEST_DATA)
; (part1 PUZZLE_DATA)

(deftest part2-test
  (is (= 3 (part2 TEST_DATA)))
  (is (= 10304 (part2 PUZZLE_DATA))))