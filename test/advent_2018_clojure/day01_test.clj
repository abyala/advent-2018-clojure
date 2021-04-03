(ns advent-2018-clojure.day01-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day01 :refer :all]))

(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day01_data.txt"))

(deftest part1-test
  (is (= 3 (part1 "+1\n+1\n+1")))
  (is (= 0 (part1 "+1\n+1\n-2")))
  (is (= 466 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 2 (part2 "+1\n-2\n+3\n+1")))
  (is (= 750 (part2 PUZZLE_DATA))))