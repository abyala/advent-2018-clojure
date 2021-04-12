(ns advent-2018-clojure.circular-list-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.circular-list :as clist]))

(deftest insert-and-seq-test
  (testing "Simple empty list"
    (is (empty? (clist/seq clist/empty-list))))
  (testing "Insert at the end"
    (is (= '(1 2 3)
           (-> clist/empty-list
               (clist/insert 1)
               (clist/insert 2)
               (clist/insert 3)
               (clist/seq)))))
  (testing "Insert in the middle"
    (is (= '(1 3 2)
           (-> clist/empty-list
               (clist/insert 1)
               (clist/insert 2)
               (clist/insert 3 1)
               (clist/seq)))))
  (testing "Insert at the front - moves the head"
    (is (= '(3 1 2)
           (-> clist/empty-list
               (clist/insert 1)
               (clist/insert 2)
               (clist/insert 3 0)
               (clist/seq)))))
  (testing "Insert from the back"
    (is (= '(1 2 4 3)
           (-> clist/empty-list
               (clist/insert 1)
               (clist/insert 2)
               (clist/insert 3)
               (clist/insert 4 -1)
               (clist/seq))))))

(deftest get-test
  (testing "Nothing from empty list"
    (is (nil? (get clist/empty-list 0)))
    (is (nil? (get clist/empty-list 1)))
    (is (nil? (get clist/empty-list -1))))
  (testing "Non-empty list"
    (let [c (-> clist/empty-list
                (clist/insert :a)
                (clist/insert :b))]
      (is (= :a (clist/get c 0)))
      (is (= :b (clist/get c 1)))
      (is (= :a (clist/get c 2)))
      #_(is (= :a (clist/get c -1)))))) ; THINK HERE

(deftest remove-test
  (testing "Empty clists are easy"
    (are [n] (is (= clist/empty-list
                    (clist/remove clist/empty-list n)))
             0
             1
             -1))
  (testing "Remove from non-negative number"
    (are [n expect] (= expect
                       (-> clist/empty-list
                           (clist/insert :a)
                           (clist/insert :b)
                           (clist/insert :c)
                           (clist/remove n)
                           (clist/seq)))
                    0 '(:b :c)
                    1 '(:a :c)
                    2 '(:a :b)
                    3 '(:b :c))
    )
  (testing "Remove from negative number"
    (are [n expect] (= expect
                       (-> clist/empty-list
                           (clist/insert :a)
                           (clist/insert :b)
                           (clist/insert :c)
                           (clist/remove n)
                           (clist/seq)))
                    -1 '(:a :b)
                    -2 '(:a :c)
                    -3 '(:b :c))))

(deftest rotate-test
  (testing "Empty clist"
    (are [n] (= clist/empty-list
                (clist/rotate clist/empty-list n))
             0
             1
             2
             -1))
  (testing "Populated clist"
    (let [c (-> clist/empty-list
                (clist/insert :a)
                (clist/insert :b)
                (clist/insert :c))]
      (are [n expect] (= expect
                         (-> c (clist/rotate n) clist/seq))
                      0 '(:a :b :c)
                      1 '(:b :c :a)
                      2 '(:c :a :b)
                      3 '(:a :b :c)
                      -1 '(:c :a :b)
                      -2 '(:b :c :a)
                      -3 '(:a :b :c)))))