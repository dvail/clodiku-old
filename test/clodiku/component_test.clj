(ns clodiku.component-test)

(defrecord Pos [x y])

(defn comp-time
  [comp]
  (loop [pos comp]
    (if (= (:x pos) 10000)
      1
      (recur (assoc pos :x (+ 1 (:x pos)) :y (+ 2 (:y pos)))))))

(defn test-record
  []
  (comp-time (->Pos 0 0)))

(defn test-map
  []
  (comp-time {:x 0 :y 0}))

