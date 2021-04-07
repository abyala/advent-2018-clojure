(ns advent-2018-clojure.day07-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day07 :refer :all]))

(def TEST_DATA "Step C must be finished before step A can begin.\nStep C must be finished before step F can begin.\nStep A must be finished before step B can begin.\nStep A must be finished before step D can begin.\nStep B must be finished before step E can begin.\nStep D must be finished before step E can begin.\nStep F must be finished before step E can begin.\n")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day07_data.txt"))

(deftest parse-dependency-test
  (is (= [\D \E] (parse-dependency "Step D must be finished before step E can begin."))))

(deftest part1-test
  (is (= "CABDFE" (part1 TEST_DATA)))
  (is (= "GNJOCHKSWTFMXLYDZABIREPVUQ" (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 15 (part2 TEST_DATA 2 0)))
  (is (= 886 (part2 PUZZLE_DATA 5 60))))