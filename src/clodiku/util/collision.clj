(ns clodiku.util.collision
  (:import (com.badlogic.gdx.math Circle)
           (com.badlogic.gdx.maps.tiled TiledMap))
  (:require [brute.entity :as be]
            [clodiku.util.entities :as eu]))

(defn get-movement-circle
  "Given an attempted movement, check the map and other entities for collisions and
  return a Circle representing the actual movement taken"
  [system ^Circle circle move]
  (let [entity-mov-x (Circle. (+ (.x circle) (:x move)) (:y move) (.radius circle))
        entity-mov-y (Circle. (+ (.y circle) (:y move)) (:x move) (.radius circle))
        map-objects (-> ^TiledMap (eu/get-current-map system)
                           (.getLayers)
                           (.get "collision")
                           (.getObjects))
        collide-x (reduce (fn [collide object]
                            ) false map-objects)]
    (Circle.
      (+ (.x circle) (:x move))
      (+ (.y circle) (:y move))
      (.radius circle))))
