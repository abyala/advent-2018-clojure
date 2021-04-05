(ns advent-2018-clojure.day04-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day04 :refer :all]
            [clojure.string :as str]))

(def TEST_DATA "[1518-11-01 00:00] Guard #10 begins shift\n[1518-11-01 00:05] falls asleep\n[1518-11-01 00:25] wakes up\n[1518-11-01 00:30] falls asleep\n[1518-11-01 00:55] wakes up\n[1518-11-01 23:58] Guard #99 begins shift\n[1518-11-02 00:40] falls asleep\n[1518-11-02 00:50] wakes up\n[1518-11-03 00:05] Guard #10 begins shift\n[1518-11-03 00:24] falls asleep\n[1518-11-03 00:29] wakes up\n[1518-11-04 00:02] Guard #99 begins shift\n[1518-11-04 00:36] falls asleep\n[1518-11-04 00:46] wakes up\n[1518-11-05 00:03] Guard #99 begins shift\n[1518-11-05 00:45] falls asleep\n[1518-11-05 00:55] wakes up")
(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day04_data.txt"))

(deftest part1-test
  (is (= 240 (part1 TEST_DATA)))
  (is (= 67558 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 4455 (part2 TEST_DATA)))
  (is (= 78990 (part2 PUZZLE_DATA))))
