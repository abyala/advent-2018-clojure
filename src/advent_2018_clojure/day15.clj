(ns advent-2018-clojure.day15
  (:require [advent-2018-clojure.utils :as utils]
            [advent-2018-clojure.point :as point]))

;; State: {:turn 0
;;         :units {name {:type     :elf/:goblin
;;                       :health   200
;;                       :location [x y]}}
;;         :locations {[x y] :empty/unit-name}


; NOTE: This works for the regular data, but does not complete for the puzzle data.

(def attack-strength 3)

(defn add-unit [c n]
  {:id n :type (case c \G :goblin :elf) :health 200})

(defn parse-input [input]
  (first (reduce (fn [[m n] [p c]] (case c
                                     \# [m n]
                                     \. [(assoc m p :empty) n]
                                     [(assoc m p (add-unit c n)) (inc n)]))
                 [{} 0]
                 (utils/parse-to-char-coords input))))

(defn unit-at [state loc]
  (let [v (state loc)]
    (when (:id v) v)))

(defn loc-of [state id]
  (->> state
       (filter #(= id (:id (second %))))
       ffirst))

(defn enemy-of [{unit-type :type}]
  ({:goblin :elf, :elf :goblin} unit-type))

(defn unit-locations [state]
  (filter (partial unit-at state) (keys state)))

(defn unit-initiative [state]
  (->> (unit-locations state)
       (map #(hash-map :loc % :id (->> % (unit-at state) :id)))
       (sort-by (comp point/point-sort :loc))
       (map :id)))

(defn neighbors-of-type [state loc unit-type]
  (keep #(let [neighbor (state %)]
           (when (= unit-type (:type neighbor))
             [% neighbor]))
        (point/adjacent-points loc)))

(defn neighbors-of-type? [state loc unit-type]
  (seq (neighbors-of-type state loc unit-type)))

(defn enemies-in-range [state loc]
  (neighbors-of-type state loc (enemy-of (unit-at state loc))))

(defn enemies-in-range? [state loc]
  (seq (enemies-in-range state loc)))

(defn empty-neighbors [state loc]
  (->> (point/adjacent-points loc)
       (filter state)
       (remove (partial unit-at state))
       (sort-by point/point-sort)))

(defn empty-paths
  ([state loc] (empty-paths state nil loc))
  ([state step0 step1] (map #(vector (or step0 %) %) (empty-neighbors state step1))))

#_(defn weakest-adjacent-enemy [state name]
    (->> (adjacent-of-type state (location-of state name) (enemy-of state name))
         (sort-by (juxt :health (comp point/point-sort :location)))
         first
         :id))

#_(defn move-unit [state name]
    (if (adjacent-enemies? state name)
      state
      (let [location (location-of state name)
            enemy-type (enemy-of state name)]
        (loop [options (possible-neighbors state location), seen #{}]
          (if-not (seq options)
            state
            (let [[chosen-loc first-step distance-so-far] (first options)]
              (if (adjacent-of-type? state chosen-loc enemy-type)
                ; We've made up our minds
                (do
                  (println "Moving" name " from" location "to" first-step)
                  (-> state
                      (assoc-in [:units name :location] first-step)
                      (update :locations merge {location :empty, first-step name})))
                #_(-> state
                      (assoc-in [:units name :location] first-step)
                      (update :locations merge {location :empty, first-step name}))
                (let [next-seen (conj seen chosen-loc)
                      next-options (into (vec (rest options))
                                         (remove #(next-seen (first %))
                                                 (possible-neighbors state chosen-loc first-step distance-so-far)))]
                  (recur next-options next-seen)))))))))

#_(defn move-unit [state loc]
    (let [unit (unit-at state loc)
          enemy-type (enemy-of unit)]
      (loop [options [[nil loc]], seen #{}]
        (if-some [[step0 step1] (first options)]
          (if (neighbors-of-type? state step1 enemy-type)
            (if (= step1 loc)
              state
              (do
                (println "Moving" unit "from" loc "to" step1 "via" step0)
                (assoc state loc :empty step0 unit)))
            (recur (into (-> options rest vec)
                         (->> (empty-neighbors state step1)
                              (remove #(seen (second %)))
                              (map #(vector (or step0 %) %))))
                   (conj seen step1)))
          state))))

(defn move-unit [state loc]
  (if (enemies-in-range? state loc)
    state
    (let [unit (unit-at state loc)
          enemy (enemy-of unit)]
      (loop [options (empty-paths state loc), seen #{}]
        (if-some [[step0 step1] (first options)]
          (if (neighbors-of-type? state step1 enemy)
            (do
              (println "Moving" unit "from" loc "to" step1 "via" step0)
              (assoc state loc :empty step0 unit))
            (recur (into (-> options rest vec)
                         (remove #(seen (second %))
                                 (empty-paths state step0 step1)))
                   (conj seen step1)))
          state)))))

(defn move-unit [state id]
  (let [loc (loc-of state id)]
    (if (enemies-in-range? state loc)
      state
      (let [unit (unit-at state loc)
            enemy (enemy-of unit)]
        (loop [options (empty-paths state loc), seen #{}]
          (if-some [[step0 step1] (first options)]
            (if (neighbors-of-type? state step1 enemy)
              (do
                (println "Moving" unit "from" loc "to" step1 "via" step0)
                (assoc state loc :empty step0 unit))
              (recur (into (-> options rest vec)
                           (remove #(seen (second %))
                                   (empty-paths state step0 step1)))
                     (conj seen step1)))
            state))))))

#_(defn remove-unit [state name]
    (if-let [loc (location-of state name)]
      (-> state
          (update :locations merge {loc :empty})
          (update :units dissoc name))
      state))

(defn weakest-victim [state id]
  (->> (loc-of state id)
       (enemies-in-range state)
       (sort-by (juxt (comp :health second)
                      (comp point/point-sort first)))
       first
       second
       :id))

#_(defn take-damage [state name]
  (let [new-health (-> state :units name :health (- attack-strength) (max 0))]
    (if (zero? new-health)
      (remove-unit state name)
      (assoc-in state [:units name :health] new-health))))

(defn take-damage [state id]
  (let [loc (loc-of state id)
        unit (unit-at state loc)
        health' (-> unit :health (- attack-strength) (max 0))]
    (if (pos-int? health')
      (assoc-in state [loc :health] health')
      (assoc state loc :empty))))

(defn attack [state id]
  (if-some [target-id (weakest-victim state id)]
    (take-damage state target-id)
    state))

(defn take-unit-turn [state id]
  (if (loc-of state id)
    (-> state (move-unit id) (attack id))
    state))

(defn combat-continues? [state]
  (->> (unit-locations state)
       (map (comp :type state))
       set
       count
       (= 2)))

(defn take-turn [state]
  (when (combat-continues? state)
    (reduce #(take-unit-turn %1 %2) state (unit-initiative state)))

  #_(let [next-state (reduce #(take-unit-turn %1 %2) state (unit-initiative state))]
    (when (not= state next-state)
      next-state)))

#_(defn take-turn [state]
  (reduce #(take-unit-turn %1 %2)
          (update state :turn inc)
          (unit-initiative state)))

#_(defn take-turn [state]
  (reduce (fn [state name]
            (if (combat-continues? state)
              (take-unit-turn state name)
              (reduced (assoc state :blocked true))))
          (-> state
              (update :turn inc)
              (assoc :blocked false))
          (unit-initiative state)))

#_(defn take-turn [state]
  (let [next-state (reduce (fn [state name]
                             (if (combat-continues? state)
                               (take-unit-turn state name)
                               (reduced (assoc state :blocked true))))
                           (-> state
                               (update :turn inc)
                               (assoc :blocked false))
                           (unit-initiative state))]
    (when (not= (:units state) (:units next-state))
      next-state)))

; TODO: Change the intermediate state to check if there are any enemies, not just the state



#_(defn go-to-war [state]
  (->> (iterate take-turn state)
       (take-while some?)
       last))

#_(defn go-to-war [state]
  (->> (iterate take-turn state)
       (drop-while combat-continues?)
       first))

#_(defn go-to-war [state]
  (->> (iterate take-turn state)
       (take-while #(not (:blocked %)))
       last))

#_(defn part1 [input]
  (let [final-state (-> input parse-input go-to-war)]
    (* (:turn final-state)
       (apply + (map (comp :health second) (:units final-state))))))

(defn war-history [initial-state]
  (->> (iterate take-turn initial-state)
       (take-while some?)))

(defn num-full-turns [history]
  (if-let [first-blocked (->> history (drop-while #(not (:blocked %))) first)]
    (dec (:turn first-blocked))
    (dec (count history))))

#_(defn hit-points-remaining [history]
  (->> history
       (take-while some?)
       last
       :units
       (map (comp :health second))
       (apply +)))

(defn hit-points-remaining [state]
  (->> (unit-locations state)
       (map (comp :health (partial unit-at state)))
       (apply +)))

#_(defn part1 [input]
  (let [history (-> input parse-input war-history)]
    (* (num-full-turns history)
       (hit-points-remaining history))))

(defn part1 [input]
  (let [history (-> input parse-input war-history)
        complete-turns (-> history count dec)
        hit-points (hit-points-remaining (last history))]
    (println complete-turns "complete turns, with a final health of" hit-points)
    (* complete-turns hit-points)))

;#######
;#G...G#
;#..E..#
;#######

(defn print-state [state]
  (let [max-x (apply max (map ffirst state))
        max-y (apply max (map (comp second first) state))]
    (->> (group-by (comp second first) state)
         (sort-by first)
         (map second)
         (map (fn [points] (->> (sort-by ffirst points)
                                (map #(or (-> % second :type {:goblin \G :elf \E}) \.))
                                (apply str))))
         (run! println))))