(ns advent-2018-clojure.utils-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.utils :refer :all]))

(deftest char->int-test
  (is (nil? (char->int nil)))
  (is (= 5 (char->int \5)))
  (is (= 5 (char->int "5")))
  (is (= 345 (char->int "345")))
  (is (nil? (char->int "A")))
  (is (nil? (char->int "3A5"))))

(deftest str->ints-test
  (is (nil? (str->ints nil)))
  (is (= '(1) (str->ints "1")))
  (is (= '(1 1) (str->ints "11")))
  (is (= '(1 2 3) (str->ints "123")))
  (is (nil? (str->ints "A")))
  (is (nil? (str->ints "1A3"))))