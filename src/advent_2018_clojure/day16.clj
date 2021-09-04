(ns advent-2018-clojure.day16
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [advent-2018-clojure.utils :as utils]))

(defn register-op [op reg a b] (op (reg a) (reg b)))
(defn immediate-op [op reg a b] (op (reg a) b))
(def addr (partial register-op +))
(def addi (partial immediate-op +))
(def mulr (partial register-op *))
(def muli (partial immediate-op *))
(def banr (partial register-op bit-and))
(def bani (partial immediate-op bit-and))
(def borr (partial register-op bit-or))
(def bori (partial immediate-op bit-or))
(defn setr [reg a _] (reg a))
(defn seti [_ a _] a)
(defn compare-op [op a b] (if (op a b) 1 0))
(defn compare-op-imm-reg [op reg a b] (compare-op op a (reg b)))
(defn compare-op-reg-imm [op reg a b] (compare-op op (reg a) b))
(defn compare-op-reg-reg [op reg a b] (compare-op op (reg a) (reg b)))
(def gtir (partial compare-op-imm-reg >))
(def gtri (partial compare-op-reg-imm >))
(def gtrr (partial compare-op-reg-reg >))
(def etir (partial compare-op-imm-reg =))
(def etri (partial compare-op-reg-imm =))
(def etrr (partial compare-op-reg-reg =))

(def all-operations [addr addi mulr muli banr bani borr bori setr seti gtir gtri gtrr etir etri etrr])

(defn parse-line [line]
  (let [[_ before op a b c after] (re-find #"Before: (\[.*\])\n(\d+) (\d+) (\d+) (\d+)\nAfter:  (.*)" line)]
    {:before (edn/read-string before)
     :op (Integer/parseInt op)
     :a (Integer/parseInt a)
     :b (Integer/parseInt b)
     :c (Integer/parseInt c)
     :after (edn/read-string after)}))

(defn parse-first-part [input]
  (->> (str/split input #"\n\n")
       (map parse-line)))

(defn parse-input [input]
  (let [[samples _] (str/split (utils/dos2unix input) #"\n\n\n")]
    {:samples (parse-first-part samples)}))

(defn operator-matches [test-set]
  (let [{:keys [before _ a b c after]} test-set]
    (filter #(= after (assoc before c (% before a b))) all-operations)))

(defn part1 [input]
  (->> (parse-input input)
       :samples
       (map operator-matches)
       (filter #(>= (count %) 3))
       count))