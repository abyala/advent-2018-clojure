(ns advent-2018-clojure.day18-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day18 :refer :all]))

(def TEST_INPUT ".#.#...|#.\n.....#|##|\n.|..|...#.\n..|#.....#\n#.#|||#|#|\n...#.||...\n.|....|...\n||...#|.#|\n|.||||..|.\n...#.|..|.")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day18_data.txt"))

(deftest part1-test
  (is (= 1147 (part1 TEST_INPUT)))
  (is (= 384416 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 195776 (part2 PUZZLE_DATA))))