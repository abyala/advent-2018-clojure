(ns advent-2018-clojure.day09-test
  (:require [clojure.test :refer :all])
  (:use [advent-2018-clojure.day09 :only [part1 part2]]))

(def PUZZLE_NUM_PLAYERS 411)
(def PUZZLE_LAST_MARBLE 71170)

(deftest part1-test
  (is (= 8317 (part1 10 1618)))
  (is (= 146373 (part1 13 7999)))
  (is (= 2764 (part1 17 1104)))
  (is (= 54718 (part1 21 6111)))
  (is (= 37305 (part1 30 5807)))
  (is (= 425688 (part1 PUZZLE_NUM_PLAYERS PUZZLE_LAST_MARBLE))))

(deftest part2-test
  (is (= 3526561003 (part2 PUZZLE_NUM_PLAYERS PUZZLE_LAST_MARBLE))))
