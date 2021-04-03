(ns advent-2018-clojure.day02-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day02 :refer :all]
            [clojure.string :as str]))

(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day02_data.txt"))

(deftest any-letter-n-times?-test
  (testing "Has doubles"
    (are [word] (any-letter-n-times? word 2)
                "bababc"
                "abbcde"
                "aabcdd"
                "abcdee"))
  (testing "No doubles"
    (are [word] (not (any-letter-n-times? word 2))
                "abcdef"
                "abcccd"
                "ababab"))
  (testing "Has triples"
    (are [word] (any-letter-n-times? word 3)
                "bababc"
                "abcccd"
                "ababab"))
  (testing "No triples"
    (are [word] (not (any-letter-n-times? word 3))
                "abcdef"
                "abbcde"
                "aabcdd"
                "abcdee")))

(deftest part1-test
  (is (= 12 (part1 ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"])))
  (is (= 7105 (part1 (str/split-lines PUZZLE_DATA)))))

(deftest part2-test
  (is (= "fgij" (part2 (str/split-lines "abcde\nfghij\nklmno\npqrst\nfguij\naxcye\nwvxyz\n"))))
  (is (= "omlvgdokxfncvqyersasjziup" (part2 (str/split-lines PUZZLE_DATA)))))