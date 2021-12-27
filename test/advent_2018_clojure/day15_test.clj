(ns advent-2018-clojure.day15-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day15 :refer :all]))

(def puzzle-input (slurp "resources/advent_2018_clojure/day15_data.txt"))

(deftest part1-test
  (testing "Test data"
    (are [expected input] (= expected (part1 input))
                          27730 "#######\n#.G...#\n#...EG#\n#.#.#G#\n#..G#E#\n#.....#\n#######"
                          36334 "#######\n#G..#E#\n#E#E.E#\n#G.##.#\n#...#E#\n#...E.#\n#######"
                          39514 "#######\n#E..EG#\n#.#G.E#\n#E.##E#\n#G..#.#\n#..E#.#\n#######"
                          27755 "#######\n#E.G#.#\n#.#G..#\n#G.#.G#\n#G..#.#\n#...E.#\n#######"
                          28944 "#######\n#.E...#\n#.#..G#\n#.###.#\n#E#G#G#\n#...#G#\n#######"
                          18740 "#########\n#G......#\n#.E.#...#\n#..##..G#\n#...##..#\n#...#...#\n#.G...G.#\n#.....G.#\n#########"))

  (testing "Puzzle data"
    (is (= -1 (part1 puzzle-input)))))