(ns advent-2018-clojure.utils)

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