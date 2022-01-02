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

(defn parse-to-char-coords
  "Given an input string, returns a lazy sequence of [[x y] c] tuples of [x y] coords to each character c."
  [input]
  (->> (str/split-lines input)
       (map-indexed (fn [y line]
                      (map-indexed (fn [x c] [[x y] c]) line)))
       (apply concat)))

(defn char->int
  "Converts a numeric character into its integer value"
  [c]
  (when c
    (try (Integer/parseInt (str c))
         (catch NumberFormatException _ nil))))

(defn str->ints
  "Converts a numeric string into a sequence of integers"
  [s]
  (when s
    (let [chars (map char->int (str s))]
      (when (every? some? chars) chars))))

(defn dos2unix [text]
  (str/replace text "\r" ""))

(defn split-blank-line
  "Given an input string, returns a sequence of sub-strings, separated by a completely
  blank string. This function preserves any newlines between blank lines, and it filters
  out Windows' \"\r\" characters."
  [input]
  (-> (str/replace input "\r" "")
      (str/split #"\n\n")))

(defn parse-long [s] (Long/parseLong s))