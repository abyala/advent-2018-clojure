(ns advent-2018-clojure.circular-list
  (:refer-clojure :exclude [empty? remove count seq get]))

(def empty-list {:data         {}
                 :keys-created 0})

(defn- head-id [clist] (:head clist))
(defn- data-at [clist id] (-> clist :data id))
(defn- value-at [clist id] (-> (data-at clist id) :value))
(defn- id-left-neighbor [clist id] (-> (data-at clist id) :left))
(defn- id-right-neighbor [clist id] (-> (data-at clist id) :right))
(defn- next-key-id [{:keys [keys-created]}] (keyword (str "id" (inc keys-created))))

(defn empty? [clist] (clojure.core/empty? (clist :data)))
(defn count [clist] (clojure.core/count (clist :data)))

(defn- walk [clist offset]
  (when-not (empty? clist)
    (loop [id (head-id clist), n (mod offset (count clist))]
      (cond
        (zero? n) id
        (pos? n) (recur (id-right-neighbor clist id) (dec n))
        :else (recur (id-left-neighbor clist id) (inc n))))))

(defn- -insert [clist id value left right]
  (update clist :data (fn [data] (-> data
                                     (assoc-in [left :right] id)
                                     (assoc-in [right :left] id)
                                     (assoc id {:value value :left left :right right})))))

(defn insert
  ([clist value] (insert clist value (count clist)))
  ([clist value ^long n]
   (let [id (next-key-id clist)
         next-clist (update clist :keys-created inc)]
     (cond
       (empty? clist) (-> next-clist
                          (assoc-in [:data id] {:value value :left id :right id})
                          (assoc :head id))
       (zero? n) (let [old-head-id (head-id clist)
                       left (id-left-neighbor clist old-head-id)]
                   (-> next-clist
                       (-insert id value left old-head-id)
                       (assoc :head id)))
       :else (let [old-id (walk clist n)
                   left (id-left-neighbor clist old-id)]
               (-insert next-clist id value left old-id))))))

(defn- unless= [a b] (when (not= a b) a))

(defn seq [clist]
  (letfn [(send-next [clist head-id last-shown]
            (when-let [next-id (if (some? last-shown) (-> (id-right-neighbor clist last-shown)
                                                          (unless= head-id))
                                                      head-id)]
              (lazy-seq (cons (value-at clist next-id) (send-next clist head-id next-id)))))]
    (when-not (empty? clist)
      (send-next clist (head-id clist) nil))))

(defn get [clist n]
  (let [id (walk clist n)]
    (value-at clist id)))

(defn remove [clist n]
  (if (<= (count clist) 1)
    empty-list
    (let [id (walk clist n)
          left (id-left-neighbor clist id)
          right (id-right-neighbor clist id)]
      (-> clist
          (update :data (fn [data] (-> data
                                             (assoc-in [left :right] right)
                                             (assoc-in [right :left] left)
                                             (dissoc id))))
          (update :head #(if (= id %) right %))))))

(defn rotate [clist n]
  (if (empty? clist)
    clist
    (assoc clist :head (walk clist n))))

(defn first-index [clist f]
  (->> (seq clist)
       (keep-indexed #(when (f %2) %1))
       first))

(defn rotate-first [clist f]
  (if-let [idx (first-index clist f)]
    (rotate clist idx)
    clist))