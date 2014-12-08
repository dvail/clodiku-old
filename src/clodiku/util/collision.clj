(ns clodiku.util.collision
  (:import (com.badlogic.gdx.math Circle Intersector)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (com.badlogic.gdx.maps.objects RectangleMapObject))
  (:require [brute.entity :as be]
            [clodiku.util.entities :as eu]))

(defn intersects?
  "Tests whether or not two shapes intersect"
  [s1 s2]
  (Intersector/overlaps s1 s2))

(defn collides-with-map?
  "Tests if the entity will collide with an impassable area of the map"
  [entity-space map-objs]
  (reduce (fn [collision? object]
            (or collision? (intersects? entity-space (.getRectangle ^RectangleMapObject object)))) false map-objs))

(defn get-movement-circle
  "Given an attempted movement, check the map and other entities for collisions and
  return a Circle representing the actual movement taken"
  [system ^Circle circle move]
  (let [entity-mov-x (Circle. (+ (.x circle) (:x move)) (.y circle) (.radius circle))
        entity-mov-y (Circle. (.x circle) (+ (.y circle) (:y move))  (.radius circle))
        map-objects (-> ^TiledMap (eu/get-current-map system)
                           (.getLayers)
                           (.get "collision")
                           (.getObjects))
        collide-x? (collides-with-map? entity-mov-x map-objects)
        collide-y? (collides-with-map? entity-mov-y map-objects)
        dx (if collide-x? 0 (:x move))
        dy (if collide-y? 0 (:y move))]
    (Circle.
      (+ (.x circle) dx)
      (+ (.y circle) dy)
      (.radius circle))))
