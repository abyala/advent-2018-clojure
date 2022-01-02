(ns advent-2018-clojure.day24-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day24 :refer :all]))

(def test-input (slurp "resources/advent_2018_clojure/day24_sample_data.txt"))
(def puzzle-input (slurp "resources/advent_2018_clojure/day24_data.txt"))

(deftest part1-test
  (are [expected input] (= expected (part1 input))
                        5216 test-input
                        35947 puzzle-input))

(deftest part2-test
  (are [expected input] (= expected (part2 input))
                        51 test-input
                        1105 puzzle-input))