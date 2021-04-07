(ns advent-2018-clojure.day07
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [advent-2018-clojure.utils :as utils]))

(defn parse-dependency
  "Parse a single instruction into a vector of [p q] where step \"p\" leads to step \"q\""
  [line]
  ; No need for regex here when the steps are named as single characters
  [(get line 5) (get line 36)])

(defn parse-dependency-sets
  "Parse the input text into a map of each step to the set of steps it depends on."
  [input]
  (let [rules (map parse-dependency (str/split-lines input))
        step-names (-> rules flatten set)
        no-deps (apply assoc {} (interleave step-names (repeat #{})))]
    (reduce (fn [acc [p q]] (update acc q #(conj % p)))
            no-deps
            rules)))

(defn fastest-work
  "Given a map of work to be done, in the form {step-name time-remaining}, returns a map
  in the form {:time t, :work w} representing the smallest amount of time remaining, and a
  set of all work steps that take that long."
  [all-work]
  (->> (group-by second all-work)
       (sort-by first)
       (map (fn [[t w]] {:time t :work (->> w (map first) set)}))
       first))

(defn advance-time
  "Given an application state, lets the fastest workers complete their current work."
  [{:keys [working] :as state}]
  (if (empty? working)
    state
    (let [{:keys [time work]} (fastest-work working)]
      (-> state
          (update :working (fn [w] (apply dissoc w work)))               ; Work complete
          (update :working (fn [w] (utils/update-values w #(- % time)))) ; Everyone else spends time
          (update :path #(concat % (-> work sort)))                      ; Apply output to the path
          (update :seen #(set/union % work))                             ; Completed work has been "seen"
          (update :time-elapsed #(+ % time))                             ; Advance the clock
          (update :num-idle #(+ % (count work)))))))                     ; Previous workers are now idle.

(defn step-cost
  "Calculates the amount of time it takes to complete a step of work."
  [name default-cost]
  (-> default-cost
      (- (dec (int \A)))
      (+ (int name))))

(defn make-assignments
  "Given an application state, assigns the highest priority work to as many idle workers as possible."
  [{:keys [deps-left seen num-idle] :as state} default-cost]
  (let [assignments (->> deps-left
                         (keep (fn [[c deps]] (when (set/subset? deps seen) c)))
                         (sort)
                         (take num-idle))
        assignment-costs (reduce #(assoc %1 %2 (step-cost %2 default-cost))
                                 {}
                                 assignments)]
    (-> state
        (update :working #(merge % assignment-costs))
        (update :deps-left #(apply dissoc % assignments))
        (update :num-idle #(- % (count assignments))))))

(defn process-dependencies [input num-workers default-cost]
  (let [state {:deps-left    (parse-dependency-sets input)
               :path         []
               :seen         #{}
               :working      {}
               :time-elapsed 0
               :num-idle     num-workers}]
    (->> (iterate #(-> % (make-assignments default-cost) advance-time) state)
         (filter #(empty? (% :deps-left)))
         first)))

(defn part1 [input]
  (->> (process-dependencies input 1 0) :path (apply str)))

(defn part2 [input num-workers default-cost]
  (->> (process-dependencies input num-workers default-cost) :time-elapsed))