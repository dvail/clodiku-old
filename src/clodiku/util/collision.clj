(ns clodiku.util.collision
  (:import (com.badlogic.gdx.math Circle)))

(defn get-movement-circle
  "Given an attempted movement, check the map and other entities for collisions and
  return a Circle representing the actual movement taken"
  [system ^Circle circle move]
  (Circle.
    (+ (.x circle) (:x move))
    (+ (.y circle) (:y move))
    (.radius circle)))
