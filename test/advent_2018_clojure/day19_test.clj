(ns advent-2018-clojure.day19-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day19 :refer :all]))

(def TEST_INPUT "#ip 0\nseti 5 0 1\nseti 6 0 2\naddi 0 1 0\naddr 1 2 3\nsetr 1 0 0\nseti 8 0 4\nseti 9 0 5")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day19_data.txt"))

(deftest part1-test
  (is (= 6 (part1 TEST_INPUT)))
  (is (= 1922 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 22302144 (part2 10551376))))
