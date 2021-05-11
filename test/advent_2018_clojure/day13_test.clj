(ns advent-2018-clojure.day13-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day13 :refer :all]))

(def TEST_DATA_1 (slurp "resources/advent_2018_clojure/day13_test_data1.txt"))
(def TEST_DATA_2 (slurp "resources/advent_2018_clojure/day13_test_data2.txt"))
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day13_data.txt"))

(deftest parse-input-test
  ; This is a dummy track whose intersection makes no sense, but it's fine for the test
  ; /<>\
  ; ^  v
  ; \-+/
  (is (= {:cells {[0 0] :slash
                  [3 0] :backslash
                  [0 2] :backslash
                  [2 2] :intersection
                  [3 2] :slash}
          :carts {[1 0] (->Cart [1 0] :west :left)
                  [2 0] (->Cart [2 0] :east :left)
                  [0 1] (->Cart [0 1] :north :left)
                  [3 1] (->Cart [3 1] :south :left)}
          :crashes []}
         (parse-input "/<>\\\n^  v\n\\-+/"))))

(deftest part1-test
  (is (= [7 3] (part1 TEST_DATA_1)))
  (is (= [16 45] (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= [6 4] (part2 TEST_DATA_2)))
  (is (= [21 91] (part2 PUZZLE_DATA))))