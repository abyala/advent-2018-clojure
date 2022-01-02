(ns advent-2018-clojure.day24
  (:require [advent-2018-clojure.utils :as utils :refer [parse-long]]
            [clojure.string :as str]))

(defn parse-modifiers [modifiers]
  (->> (str/split modifiers #";")
       (mapcat (fn [section] (let [[t _ & values] (re-seq #"\w+" section)]
                               (map #(vector (keyword %) (keyword t)) values))))
       (into {})))

(defn parse-group [line]
  (let [parsed (or (re-matches #"(\d+) units each with (\d+) hit points \((.*)\) with an attack that does (\d+) (\w+) damage at initiative (\d+)" line)
                   (let [[_ a b d e f] (re-matches #"(\d+) units each with (\d+) hit points with an attack that does (\d+) (\w+) damage at initiative (\d+)" line)]
                     [_ a b "" d e f]))
        [_ num-units hit-points modifiers damage dmg-type initiative] parsed]
    {:units         (parse-long num-units),
     :hit-points    (parse-long hit-points),
     :modifiers     (parse-modifiers modifiers)
     :attack-damage (parse-long damage)
     :attack-type   (keyword dmg-type)
     :initiative    (parse-long initiative)}))

(defn parse-team [team lines]
  (->> (rest (str/split-lines lines))
       (map-indexed (fn [idx line] (-> (parse-group line)
                                       (assoc :team team)
                                       (assoc :id (str (subs (str team) 1) " group " (inc idx))))))))

(defn parse-input [input]
  (let [[immune infection] (utils/split-blank-line input)]
    (reduce (fn [acc group] (assoc acc (:id group) group))
            {}
            (concat (parse-team :immune immune)
                    (parse-team :infection infection)))))

(defn effective-power [group]
  (* (:units group) (:attack-damage group)))

(defn incoming-damage-per-unit [group attack-type attack-damage]
  (case (get-in group [:modifiers attack-type])
    :immune 0
    :weak (* attack-damage 2)
    attack-damage))

(defn select-target [{:keys [attack-type attack-damage team]} groups]
  (when-some [selected (->> groups
                            (filter #(not= team (:team %)))
                            (sort-by (juxt (comp - #(incoming-damage-per-unit % attack-type attack-damage))
                                           (comp - effective-power)
                                           (comp - :initiative)))
                            first)]
    (when (pos-int? (incoming-damage-per-unit selected attack-type attack-damage))
      selected)))

(defn select-targets [groups]
  (->> (reduce (fn [[attacks unchosen] attacker]
                 (if-some [target (select-target attacker (vals unchosen))]
                   [(conj attacks [(:id attacker) (:id target)]) (dissoc unchosen (:id target))]
                   [attacks unchosen]))
               [[] groups]
               (sort-by (juxt (comp - effective-power) (comp - :initiative)) (vals groups)))
       first
       (into {})))

(defn take-damage [defender attacker]
  (let [dmg-per-unit (incoming-damage-per-unit defender (:attack-type attacker) (:attack-damage attacker))
        dmg-attempted (* dmg-per-unit (:units attacker))
        units-killed (quot dmg-attempted (:hit-points defender))
        units-remaining (- (:units defender) units-killed)]
    (when (pos-int? units-remaining)
      (assoc defender :units units-remaining))))

(defn attack-targets [groups attack-plans]
  (reduce (fn [acc attacker-id]
            (let [attacker (acc attacker-id)
                  defender-id (attack-plans attacker-id)
                  defender (acc defender-id)]
              (if (every? some? [attacker defender])
                (if-let [defender' (take-damage defender attacker)]
                  (assoc acc defender-id defender')
                  (dissoc acc defender-id))
                acc)))
          groups
          (map :id (sort-by (juxt (comp - :initiative) (comp - effective-power)) (vals groups)))))

(defn battle-rages? [groups]
  (= 2 (->> groups vals (map :team) set count)))

(defn take-turn [groups]
  (when (battle-rages? groups)
    (let [attack-plan (select-targets groups)
          groups' (attack-targets groups attack-plan)]
      (when (not= groups groups') groups'))))

(defn turn-seq [groups]
  (->> (iterate take-turn groups)
       (take-while some?)))

(defn final-score [groups]
  {:winner (if (battle-rages? groups) :nobody (-> groups vals first :team))
   :units (reduce + (map :units (vals groups)))})

(defn boost-immune-system [groups boost-by]
  (reduce-kv (fn [m k v] (if (= :immune (:team v))
                           (update-in m [k :attack-damage] + boost-by)
                           m))
             groups
             groups))

(defn fight-to-the-death [groups]
  (-> groups turn-seq last final-score))

(defn part1 [input]
  (-> input parse-input fight-to-the-death :units))

(defn part2 [input]
  (let [unboosted (parse-input input)]
    (->> (range)
         (map (comp fight-to-the-death #(boost-immune-system unboosted %)))
         (keep #(when (= :immune (:winner %)) (:units %)))
         first)))