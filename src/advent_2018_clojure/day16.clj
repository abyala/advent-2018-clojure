(ns advent-2018-clojure.day16
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [advent-2018-clojure.utils :as utils]))

(defn register-op [op reg a b] (op (reg a) (reg b)))
(defn immediate-op [op reg a b] (op (reg a) b))
(def addr (partial register-op  +))
(def addi (partial immediate-op +))
(def mulr (partial register-op  *))
(def muli (partial immediate-op *))
(def banr (partial register-op  bit-and))
(def bani (partial immediate-op bit-and))
(def borr (partial register-op  bit-or))
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

(defn parse-sample [sample]
  (let [[_ before op a b c after] (re-find #"Before: (\[.*\])\n(\d+) (\d+) (\d+) (\d+)\nAfter:  (.*)" sample)]
    {:before (edn/read-string before)
     :op     (Integer/parseInt op)
     :a      (Integer/parseInt a)
     :b      (Integer/parseInt b)
     :c      (Integer/parseInt c)
     :after  (edn/read-string after)}))

(defn parse-samples [input]
  (->> (str/split input #"\n\n")
       (map parse-sample)))

(defn parse-test-program [input]
  (->> (str/split-lines input)
       (map #(edn/read-string (str "[" % "]")))))

(defn parse-input [input]
  (let [[samples test-program] (str/split (utils/dos2unix input) #"\n\n\n\n")]
    {:samples (parse-samples samples)
     :test-program (parse-test-program test-program)}))

(defn run-operation [registers op a b c]
  (assoc registers c (op registers a b)))

(defn operator-matches
  ([sample] (operator-matches sample all-operations))
  ([sample ops] (let [{:keys [before _ a b c after]} sample]
                  (->> ops
                       (filter #(= after (run-operation before % a b c)))
                       set))))

(defn part1 [input]
  (->> (parse-input input)
       :samples
       (map operator-matches)
       (filter #(>= (count %) 3))
       count))

(defn initial-solve-opcodes [samples]
  (reduce (fn [possibilities sample] (update possibilities (:op sample) #(operator-matches sample %)))
          (into {} (map vector (range 16) (repeat (set all-operations))))
          samples))

(defn algorithm-known? [[_ algs]]
  (= 1 (count algs)))

(defn solve-opcodes [samples]
  (loop [unsolved (initial-solve-opcodes samples) solved {}]
    (if-let [[id algs] (first (filter algorithm-known? unsolved))]
      (let [alg (first algs)]
        (recur (-> (dissoc unsolved id)
                   (utils/update-values #(disj % alg)))
               (assoc solved id alg)))
      solved)))

(defn run-program [opcodes test-program]
  (reduce (fn [registers [op a b c]] (run-operation registers (opcodes op) a b c))
          [0 0 0 0]
          test-program))

(defn part2 [input]
  (let [{:keys [samples test-program]} (parse-input input)
        opcodes (solve-opcodes samples)
        registers (run-program opcodes test-program)]
    (first registers)))
