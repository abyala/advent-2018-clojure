(ns advent-2018-clojure.day20-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day20 :refer :all]))

(def EXAMPLE_1 "^WNE$")
(def EXAMPLE_2 "^ENWWW(NEEE|SSE(EE|N))$")
(def EXAMPLE_3 "^ENNWSWW(NEWS|)SSSEEN(WNSE|)EE(SWEN|)NNN$")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day20_data.txt"))

(deftest part1-test
  (are [expected input] (= expected (part1 input))
                        3 EXAMPLE_1
                        10 EXAMPLE_2
                        18 EXAMPLE_3
                        3839 PUZZLE_DATA))

(deftest part2-test
  (is (= 8407 (part2 PUZZLE_DATA))))
