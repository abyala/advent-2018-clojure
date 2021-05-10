(ns advent-2018-clojure.utils
  (:require [clojure.string :as str]))

(defn update-values
  "Thank you to Jay Fields' post for this awesome way to apply a function
  to every element of a map.
  http://blog.jayfields.com/2011/08/clojure-apply-function-to-each-value-of.html"
  [m f & args]
  (reduce (fn [r [k v]] (assoc r k (apply f v args))) {} m))

(defn unless=
  "Returns the first value, or nil if it matches the second value"
  [a b]
  (when (not= a b) a))

(defn mapcat-indexed
  "Applies a mapcat to a function on a collection, where f takes arguments [index value]"
  [f coll]
  (apply concat (map-indexed f coll)))

(defn parse-to-coord-map
  "Given an input string, returns a map of [x y] coordinates to each character"
  ([input] (parse-to-coord-map input any?))
  ([input f] (->> (str/split-lines input)
                  (mapcat-indexed (fn [y line]
                                    (->> line
                                         (keep-indexed (fn [x c] (when (f c) [[x y] c])))
                                         (apply concat))))
                  (apply array-map))))