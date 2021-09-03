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

(defn add-unit [state type loc]
  (let [unit-type (case type \G "goblin" \E "elf")
        id (keyword (str unit-type "-" (count (:units state))))]
    (-> state
        (update :units assoc id {:id       id
                                 :type     (keyword unit-type)
                                 :health   200
                                 :location loc})
        (update :locations assoc loc id))))

(defn unit-at [state loc]
  ((:locations state) loc))
(defn unit-named [state name]
  (-> state :units name))
(defn location-of
  ([unit] (:location unit))
  ([state name] (location-of (unit-named state name))))
(defn type-of
  ([unit] (:type unit))
  ([state name] (type-of (unit-named state name))))
(defn enemy-of
  ([unit-type] ({:goblin :elf, :elf :goblin} unit-type))
  ([state name] (enemy-of (type-of state name))))

(defn parse-input [input]
  (reduce (fn [state [p c]]
            (case c
              \# state
              \. (update state :locations assoc p :empty)
              (add-unit state c p)))
          {:turn 0 :units {} :locations {}}
          (utils/parse-to-char-coords input)))

(defn unit-initiative [state]
  (->> (:units state)
       (sort-by (comp point/point-sort :location second))
       (map first)))

(defn adjacent-of-type [state loc type]
  (->> (point/adjacent-points loc)
       (keep #(unit-at state %))
       (map #(-> state :units %))
       (filter #(= type (:type %)))))

(defn adjacent-of-type? [state loc enemy-type]
  (seq (adjacent-of-type state loc enemy-type)))

(defn adjacent-enemies [state name]
  (adjacent-of-type state (location-of state name) (enemy-of state name)))

(defn adjacent-enemies? [state name]
  (seq (adjacent-enemies state name)))

(defn possible-neighbors
  ([state loc] (possible-neighbors state loc nil 0))
  ([state loc first-step distance-so-far] (->> (point/adjacent-points loc)
                                               (keep #(when (= :empty (unit-at state %))
                                                        [% (or first-step %) (inc distance-so-far)])))))

(defn weakest-adjacent-enemy [state name]
  (->> (adjacent-of-type state (location-of state name) (enemy-of state name))
       (sort-by (juxt :health (comp point/point-sort :location)))
       first
       :id))

(defn move-unit [state name]
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
              (-> state
                  (assoc-in [:units name :location] first-step)
                  (update :locations merge {location :empty, first-step name}))
              (let [next-seen (conj seen chosen-loc)
                    next-options (into (vec (rest options))
                                         (remove #(next-seen (first %))
                                                 (possible-neighbors state chosen-loc first-step distance-so-far)))]
                (recur next-options next-seen)))))))))

(defn remove-unit [state name]
  (if-let [loc (location-of state name)]
    (-> state
        (update :locations merge {loc :empty})
        (update :units dissoc name))
    state))

(defn take-damage [state name]
  (let [new-health (-> state :units name :health (- attack-strength) (max 0))]
    (if (zero? new-health)
      (remove-unit state name)
      (assoc-in state [:units name :health] new-health))))

(defn attack [state name]
  (if-let [target-id (weakest-adjacent-enemy state name)]
    (take-damage state target-id)
    state))

(defn take-unit-turn [state name]
  (if (unit-named state name)
    (-> state (move-unit name) (attack name))
    state))

(defn take-turn [state]
  (let [next-state (reduce #(take-unit-turn %1 %2) state (unit-initiative state))]
    (when (not= state next-state)
      (update next-state :turn inc))))

(defn combat-continues? [state]
  (->> (:units state)
       (map (comp :type second))
       set
       count
       (= 2)))

(defn take-turn [state]
  (reduce #(take-unit-turn %1 %2)
          (update state :turn inc)
          (unit-initiative state)))

(defn take-turn [state]
  (reduce (fn [state name]
            (if (combat-continues? state)
              (take-unit-turn state name)
              (reduced (assoc state :blocked true))))
          (-> state
              (update :turn inc)
              (assoc :blocked false))
          (unit-initiative state)))

(defn take-turn [state]
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



(defn go-to-war [state]
  (->> (iterate take-turn state)
       (take-while some?)
       last))

(defn go-to-war [state]
  (->> (iterate take-turn state)
       (drop-while combat-continues?)
       first))

(defn go-to-war [state]
  (->> (iterate take-turn state)
       (take-while #(not (:blocked %)))
       last))

(defn part1 [input]
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

(defn hit-points-remaining [history]
  (->> history
       (take-while some?)
       last
       :units
       (map (comp :health second))
       (apply +)))

(defn part1 [input]
  (let [history (-> input parse-input war-history)]
    (* (num-full-turns history)
       (hit-points-remaining history))))