(ns advent-2018-clojure.day14-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day14 :refer :all]))

(def PUZZLE_DATA 503761)

(deftest part1-test
  (are [num-recipes target] (= (part1 num-recipes) target)
                            5 "0124515891"
                            9 "5158916779"
                            18 "9251071085"
                            2018 "5941429882"
                            PUZZLE_DATA "1044257397"))

(deftest part2-test
  (are [digits num-recipes] (= (part2 digits) num-recipes)
                            "51589" 9
                            "01245" 5
                            "92510" 18
                            "59414" 2018))
