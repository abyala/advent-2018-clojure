(ns advent-2018-clojure.day13
  (:require [advent-2018-clojure.utils :as utils]))

(defn c->path [c] ({\\ :backslash \/ :slash \+ :intersection} c))
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
                                   c->path (assoc-in state [:cells coords] (c->path c))
                                   c->dir (assoc-in state [:carts coords] (->Cart coords (c->dir c) :left))
                                   state))
          {:cells {} :carts {} :crashes []}
          (utils/parse-to-char-coords input)))

(defn move-forward [coords dir]
  (mapv + coords (case dir
                   :north [0 -1]
                   :south [0 1]
                   :west [-1 0]
                   :east [1 0])))

(defn move-cart [state {:keys [coords dir next-intersection] :as cart}]
  (let [new-coords (move-forward coords dir)
        cell ((state :cells) new-coords)]
    (case cell
      nil (assoc cart :coords new-coords)
      :intersection (->Cart new-coords
                            (-> directions dir next-intersection)
                            (next-intersection-dir next-intersection))
      (->Cart new-coords (-> directions dir cell) next-intersection))))

(defn coord-sort [coords]
  (sort-by (juxt first second) coords))

(defn collides? [state coords]
  (some #(= coords %) (-> state :carts keys)))

(defn next-turn [state]
  (reduce (fn [next-state coords]
            (if-let [old-cart ((next-state :carts) coords)] ; Make sure cart hasn't been hit yet
              (let [cart (move-cart next-state old-cart)
                    new-coords (:coords cart)]
                (if (collides? next-state new-coords)
                  (-> next-state
                      (update :carts #(apply dissoc % [coords new-coords]))
                      (update :crashes conj new-coords))
                  (update next-state :carts #(-> (dissoc % coords)
                                                 (assoc new-coords cart)))))
              next-state))
          state
          (coord-sort (-> state :carts keys))))

(defn part1 [input]
  (->> (parse-input input)
       (iterate next-turn)
       (map :crashes)
       (filter seq)
       ffirst))

(defn part2 [input]
  (->> (parse-input input)
       (iterate next-turn)
       (map (comp keys :carts))
       (filter #(= 1 (count %)))
       ffirst))