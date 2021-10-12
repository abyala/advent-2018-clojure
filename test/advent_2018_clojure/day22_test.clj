(ns advent-2018-clojure.day22-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day22 :refer :all]))

(def TEST_DEPTH 510)
(def TEST_TARGET [10 10])
(def PUZZLE_DEPTH 3066)
(def PUZZLE_TARGET [13 726])

(deftest part1-test
  (is (= 114 (part1 TEST_DEPTH TEST_TARGET)))
  (is (= 10115 (part1 PUZZLE_DEPTH PUZZLE_TARGET))))

#_(deftest part2-test
  (is (= 45 (part2 TEST_DEPTH TEST_TARGET))))

; Estimate of 983 was too low
; Estimate of 984 was also too low, and someone else's answer
