(ns advent-2018-clojure.day22
  (:require [advent-2018-clojure.point :as point]
            [clojure.set :as set]))

(def gear-change-cost 7)
(defn create-cave [depth target] {:points {}, :target target, :depth depth})

(defn cave-depth [cave] (:depth cave))
(defn cave-target [cave] (:target cave))
(defn erosion-level [cave point] (get-in cave [:points point :erosion-level]))
(defn erosion-type [cave point] (get-in cave [:points point :erosion-type]))

(defn point-above [point] (point/move-south point))
(defn point-left [point] (point/move-west point))

(defn analyze-geologic-index [cave [x y :as point]]
  (cond
    (= [0 0] point) 0
    (= (cave-target cave) point) 0
    (zero? y) (* x 16807)
    (zero? x) (* y 48271)
    :else (apply * (map #(erosion-level cave %)
                        ((juxt point-above point-left) point)))))

(defn analyze-erosion-level [cave geo-index]
  (-> geo-index
      (+ (cave-depth cave))
      (mod 20183)))

(defn analyze-erosion-type [erosion-level]
  ([:rocky :wet :narrow] (mod erosion-level 3)))

(defn analyze-location [cave point]
  (let [geo-index (analyze-geologic-index cave point)
        ero-level (analyze-erosion-level cave geo-index)
        ero-type (analyze-erosion-type ero-level)]
    (assoc-in cave [:points point] {:geologic-index geo-index, :erosion-level ero-level, :erosion-type ero-type})))

(defn target-rectangle-points [[x y]]
  (for [x' (range (inc x))
        y' (range (inc y))]
    [x' y']))

(defn down-right [point] (-> point point/move-down point/move-east))
(defn expand-cave-to [cave point]
  (if (or (get-in cave [:points point])
          (some neg-int? point))
    cave
    (-> cave
        (expand-cave-to (point/move-up point))
        (expand-cave-to (point/move-west point))
        (analyze-location point))))

(defn analyze-cave [depth target]
  (expand-cave-to (create-cave depth target) target))

(defn risk-level [cave]
  (->> (cave-target cave)
       (target-rectangle-points)
       (map #(erosion-type cave %))
       (map {:rocky 0, :wet 1, :narrow 2})
       (apply +)))

(defn part1 [depth target]
  (risk-level (analyze-cave depth target)))

(defn print-cave [cave]
  (point/print-grid (:points cave) #({:rocky \. :wet \= :narrow \|} (:erosion-type %))))

(def accessible-gear {:rocky  #{:climbing-gear :torch}
                      :wet    #{:climbing-gear :neither}
                      :narrow #{:torch :neither}})
(def all-gear #{:climbing-gear :torch :neither})

(defn accessible? [cave point gear]
  (-> (erosion-type cave point)
      (accessible-gear)
      (contains? gear)))

(defn alternate-gear [cave point gear]
  (->> (disj all-gear gear)
       (filter #(accessible? cave point %))
       first))

(defn astar-estimate [cost target [point type]]
  (+ cost (point/distance point target) (if (= type :torch) 0 gear-change-cost)))

(defn lowest-estimate [distances target options]
  (apply min-key #(astar-estimate (distances %) target %) options))

; This is correct for the sample, but it gave a too-low cost of 983
(defn part2 [depth target]
  (loop [cave (expand-cave-to (create-cave depth target) [2 2])
         options #{[point/origin :climbing-gear]}
         distances {[point/origin :climbing-gear] 0}]
    (let [[point gear :as choice] (lowest-estimate distances target options)]
      (if (= choice [target :torch])
        (distances choice)
        (let [cave' (expand-cave-to cave (down-right point))
              dist (distances choice)
              neighbors (->> (point/adjacent-points point)
                             (remove #(some neg-int? %)))
              gear-swap-option {[point (alternate-gear cave point gear)] (+ dist gear-change-cost)}
              all-options (->> (filter #(accessible? cave' % gear) neighbors)
                               (map #(vector [% gear] (inc dist)))
                               (into gear-swap-option))
              unseen-options (remove distances (keys all-options))]
          (recur cave'
                 (apply conj (disj options choice) unseen-options)
                 (merge-with min distances all-options)))))))