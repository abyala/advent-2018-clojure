(ns advent-2018-clojure.day14-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day14 :refer :all]))

(def PUZZLE_DATA 503761)

(deftest part1-test
  (are [input expected] (= expected (part1 input))
                        9 "5158916779"
                        5 "0124515891"
                        18 "9251071085"
                        2018 "5941429882"
                        PUZZLE_DATA "1044257397"))

(deftest part2-test
  (are [input expected] (= expected (part2 input))
                        "51589" 9
                        "01245" 5
                        "92510" 18
                        "59414" 2018
                        (str PUZZLE_DATA) 20185425))