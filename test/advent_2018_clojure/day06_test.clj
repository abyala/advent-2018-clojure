(ns advent-2018-clojure.day06-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day06 :refer :all]))

(def TEST_DATA "1, 1\n1, 6\n8, 3\n3, 4\n5, 5\n8, 9")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day06_data.txt"))

(deftest perimeter-points-test
  (is (= #{[1 2] [1 3] [1 4] [2 2] [2 4] [3 2] [3 3] [3 4]}
         (set (perimeter-points [1 2] [3 4])))))

(deftest closest-test
  (let [points [[1 1] [1 6] [8 3] [3 4] [5 5] [8 9]]]
    (is (= [1 1] (closest [1 1] points)))
    (is (= [1 1] (closest [1 2] points)))
    (is (nil? (closest [5 1] points)))
    (is (nil? (closest [0 4] points)))
    (is (= [1 6] (closest [1 5] points)))))

(deftest part1-test
  (is (= 17 (part1 TEST_DATA)))
  (is (= 3894 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 16 (part2 TEST_DATA 32)))
  (is (= 39398 (part2 PUZZLE_DATA 10000))))
