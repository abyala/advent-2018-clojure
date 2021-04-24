(ns advent-2018-clojure.day12-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day12 :refer :all]))

(def TEST_DATA "initial state: #..#.#..##......###...###\n\n...## => #\n..#.. => #\n.#... => #\n.#.#. => #\n.#.## => #\n.##.. => #\n.#### => #\n#.#.# => #\n#.### => #\n##.#. => #\n##.## => #\n###.. => #\n###.# => #\n####. => #\n")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day12_data.txt"))

(deftest part1-test
  (is (= 325 (part1 TEST_DATA)))
  (is (= 1787 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 1100000000475 (part2 PUZZLE_DATA))))