(ns advent-2018-clojure.day13
  (:require [advent-2018-clojure.utils :as utils]))

(defn intersection? [c] (= \+ c))
(defn turn? [c] (#{\\ \/} c))
(defn c->turn [c] ({\\ :backslash \/ :slash} c))
(defn c->dir [c] ({\^ :north \v :south \> :east \< :west} c))
(defn next-intersection-dir [dir] ({:left     :straight
                                    :straight :right
                                    :right    :left} dir))

(defrecord Cart [coords dir next-intersection])
(def directions
  {:north {:straight :north, :left :west, :right :east, :slash :east, :backslash :west}
   :east  {:straight :east, :left :north, :right :south, :slash :north, :backslash :south}
   :south {:straight :south, :left :east, :right :west, :slash :west, :backslash :east}
   :west  {:straight :west, :left :south, :right :north, :slash :south, :backslash :north}})

(defn parse-input [input]
  (reduce (fn [state [coords c]] (condp apply [c]
                                   intersection? (assoc-in state [:cells coords] :intersection)
                                   turn? (assoc-in state [:cells coords] (c->turn c))
                                   c->dir (assoc-in state [:carts coords] (->Cart coords (c->dir c) :left))
                                   state))
          {:cells {} :carts {} :crashes []}
          (utils/parse-to-coord-map input)))

(defn move-forward [coords dir]
  (mapv + coords (case dir
                   :north [0 -1]
                   :south [0 1]
                   :west [-1 0]
                   :east [1 0])))

(defn adjust-direction [state {:keys [dir next-intersection] :as cart} new-coords]
  (let [cell ((state :cells) new-coords)]
    (case cell
      nil (assoc cart :coords new-coords)
      :intersection (->Cart new-coords
                            (-> directions dir next-intersection)
                            (next-intersection-dir next-intersection))
      (->Cart new-coords (-> directions dir cell) next-intersection))))

(defn collides? [state coords]
  (some #(= coords %) (-> state :carts keys)))

(defn move-cart [state {:keys [coords dir] :as cart}]
  (let [new-coords (move-forward coords dir)]
    (adjust-direction state cart new-coords)))

(defn coord-sort [coords]
  (sort-by (juxt first second) coords))

(defn next-turn [state]
  (reduce (fn [next-state coords]
            (let [cart (move-cart next-state ((next-state :carts) coords))
                  new-coords (:coords cart)]
              (if (collides? next-state new-coords)
                (-> next-state
                    (update :carts #(apply dissoc % [coords new-coords]))
                    (update :crashes conj new-coords))
                (update next-state :carts #(-> (dissoc % coords)
                                               (assoc new-coords cart))))))
          state
          (coord-sort (-> state :carts keys))))

(defn part1 [input]
  (let [state (parse-input input)]
    (->> (iterate next-turn state)
         (map :crashes)
         (filter seq)
         ffirst)))
