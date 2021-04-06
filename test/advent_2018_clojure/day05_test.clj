(ns advent-2018-clojure.day05-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day05 :refer :all]
            [clojure.string :as str]))

(def TEST_DATA "dabAcCaCBAcCcaDA")
(def PUZZLE_DATA (str/trim (slurp "resources/advent_2018_clojure/day05_data.txt")))


(deftest react-polymer-test
  (are [polymer result] (= result (react-polymer polymer))
                        "aA" ""
                        "abBA" ""
                        "abAB" "abAB"
                        "aabAAB" "aabAAB"
                        "dabAcCaCBAcCcaDA" "dabCBAcaDA"))

(deftest part1-test
  (is (= 10 (part1 TEST_DATA)))
  (is (= 11152 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 4 (part2 TEST_DATA)))
  (is (= 6136 (part2 PUZZLE_DATA))))