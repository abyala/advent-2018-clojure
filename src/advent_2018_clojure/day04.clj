(ns advent-2018-clojure.day04
  (:require [clojure.string :as str]))

(defn parse-event [line]
  (let [[_ minute message] (re-matches #".* \d+:(\d+)\] (.*)" line)]
    (merge {:minute (Integer/parseInt minute)}
           (or (when-let [[_ guard-id] (re-matches #"Guard \#(\d+) .*" message)]
                 {:type :new-guard :id (Integer/parseInt guard-id)})
               (when (= message "wakes up") {:type :wakes-up})
               (when (= message "falls asleep") {:type :falls-asleep})))))

(defn parse-events [data]
  (->> (str/split-lines data)
       (sort)
       (map parse-event)))

(defn parse-sleep-periods
  ([data] (parse-sleep-periods (parse-events data) nil nil))
  ([events id sleep-time] (when-let [event (first events)]
                            (case (:type event)
                              :new-guard (parse-sleep-periods (rest events) (:id event) sleep-time)
                              :falls-asleep (parse-sleep-periods (rest events) id (:minute event))
                              :wakes-up (lazy-seq (cons {:id id :start sleep-time :end (:minute event)}
                                                        (parse-sleep-periods (rest events) id sleep-time)))))))

(defn period-frequencies [periods]
  (->> periods
       (mapcat #(range (:start %) (:end %)))
       (frequencies)))

(defn parse-guards [data]
  (let [periods (parse-sleep-periods data)]
    (->> (group-by :id periods)
         (mapv (fn [[id p]] [id (period-frequencies p)]))
         (into {}))))

(defn time-asleep [guard]
  (->> (second guard)
       vals
       (apply +)))

(defn sleepiest-id [guards]
  (->> guards
       (map (fn [[id :as guard]] [id (time-asleep guard)]))
       (sort-by second >)
       ffirst))

(defn most-common-minute [freqs]
  (->> freqs
       (sort-by second >)
       ffirst))

(defn minute-most-frequently-asleep [freqs]
  (->> freqs (sort-by second >) first))

(defn part1 [data]
  (let [guards (parse-guards data)
        guard-id (sleepiest-id guards)
        most-common-minute (most-common-minute (guards guard-id))]
    (* guard-id most-common-minute)))

(defn part2 [data]
  (->> (parse-guards data)
       (map (fn [[id freqs]]
              (let [[minute freq] (minute-most-frequently-asleep freqs)]
                [(* id minute) freq])))
       (sort-by second >)
       ffirst))