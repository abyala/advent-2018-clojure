(ns advent-2018-clojure.day21
  (:require [advent-2018-clojure.wrist-device :as device]
            [advent-2018-clojure.day19 :as day19]))

(def op-with-eqrr 30)

(defn loops? [device]
  (let [freqs (->> (device/run-all-steps device)
                   (map :ip)
                   (take 10000)
                   (frequencies))]
    (= (get freqs 18) 1422)))

(defn value-at-eqrr-op [device]
  (->> (device/run-all-steps device)
       (keep #(if (= op-with-eqrr (:ip %))
                (get-in % [:registers 4])))
       first))

(defn part1 [input]
  (->> input day19/parse-device value-at-eqrr-op))

