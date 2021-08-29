(ns advent-2018-clojure.day14
  (:require [advent-2018-clojure.utils :refer [str->ints]]
            [clojure.string :as str]))

(def initial-state {:recipes [3 7], :elves [0 1]})

(defn add-next-recipe [{:keys [recipes elves] :as state}]
  (let [next-recipe (apply + (map #(recipes %) elves))]
    (update state :recipes #(apply conj % (str->ints next-recipe)))))

(defn move-elves [{:keys [recipes elves] :as state}]
  (assoc state :elves (mapv #(mod (+ % 1 (recipes %))
                                  (count recipes))
                            elves)))

(defn next-turn [state]
  (-> state add-next-recipe move-elves))

(defn part1 [input-recipes]
  (let [target-recipes (+ input-recipes 10)]
    (->> (iterate next-turn initial-state)
         (map :recipes)
         (drop-while #(< (count %) target-recipes))
         first
         (drop input-recipes)
         (take 10)
         (apply str))))

(defn num-recipes-before [state target]
  (let [recipes (:recipes state)
        num-recipes (count recipes)
        last-n (inc (count target))]
    (when (>= num-recipes last-n)
      (let [s (apply str (subvec recipes (- num-recipes last-n)))]
        (when-let [idx (str/index-of s target)]
          (+ num-recipes idx (- last-n)))))))

(defn part2 [input-recipes]
  (->> (iterate next-turn initial-state)
       (keep #(num-recipes-before % input-recipes))
       first))