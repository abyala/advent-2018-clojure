(ns advent-2018-clojure.summed-area-table)

(defn create [length f]
  (let [table (reduce (fn [acc [x y]]
                        (assoc acc [x y] (+ (f x y)
                                            (get acc [(dec x) y] 0)
                                            (get acc [x (dec y)] 0)
                                            (- (get acc [(dec x) (dec y)] 0)))))
                      {}
                      (for [y (range length)
                            x (range length)]
                        [x y]))]
    {:data   table
     :length length}))

(defn sum-of-square [sat x y length]
  (let [d (:data sat)
        size (:length sat)
        max-x (+ x (dec length))
        max-y (+ y (dec length))]
    (when (and (<= (+ x length) size)
               (<= (+ y length) size))
      (+ (d [max-x max-y])
         (get d [(dec x) (dec y)] 0)
         (- (get d [max-x (dec y)] 0))
         (- (get d [(dec x) max-y] 0))))))

