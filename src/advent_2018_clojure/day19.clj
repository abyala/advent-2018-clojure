(ns advent-2018-clojure.day19
  (:require [clojure.string :as str]
            [advent-2018-clojure.wrist-device :as device]))

(def empty-registers [0 0 0 0 0])

(defn parse-instruction [text]
  (let [[op a b c] (str/split text #" ")]
    [(keyword op) (Integer/parseInt a) (Integer/parseInt b) (Integer/parseInt c)]))

(defn parse-device [input]
  (let [[ip-text & instruction-text] (str/split-lines input)
        ip-register (Integer/parseInt (subs ip-text 4))]
    (device/create-device empty-registers
                          (mapv parse-instruction instruction-text)
                          :ip-register ip-register)))

(defn part1 [input]
  (->> (parse-device input)
       (device/run-to-completion)
       (device/registers)
       first))

; Pointless code to get the star... go and read something more interesting
(defn part2 [n]
  (let [divisors (filter #(zero? (mod n %))
                         (range 1 (inc n)))]
    (apply + divisors)))
