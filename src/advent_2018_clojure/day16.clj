(ns advent-2018-clojure.day16
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [advent-2018-clojure.wrist-device :as device]
            [advent-2018-clojure.utils :as utils]))

(def empty-registers [0 0 0 0])

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
       (mapv #(edn/read-string (str "[" % "]")))))

(defn parse-input [input]
  (let [[samples test-program] (str/split (utils/dos2unix input) #"\n\n\n\n")]
    {:samples      (parse-samples samples)
     :test-program (parse-test-program test-program)}))

(defn operator-matches
  ([sample] (operator-matches sample device/operation-ids))
  ([sample ops] (let [{:keys [before op a b c after]} sample]
                  (->> ops
                       (filter #(= after (-> (device/create-device before
                                                                   [[op a b c]]
                                                                   :operations {op (device/operation-named %)})
                                             (device/run-operation)
                                             (device/registers))))
                       set))))

(defn part1 [input]
  (->> (parse-input input)
       :samples
       (map operator-matches)
       (filter #(>= (count %) 3))
       count))

(defn initial-solve-opcodes [samples]
  (reduce (fn [possibilities sample] (update possibilities (:op sample) #(operator-matches sample %)))
          (into {} (map vector (range 16) (repeat (set device/operation-ids))))
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
  (device/run-to-completion (device/create-device empty-registers
                                                  test-program
                                                 :operations (utils/update-values opcodes device/operation-named))))

(defn part2 [input]
  (let [{:keys [samples test-program]} (parse-input input)]
    (-> (solve-opcodes samples)
        (run-program test-program)
        (device/registers)
        first)))
