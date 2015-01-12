(ns clodiku.util.collision
  (:import (com.badlogic.gdx.math Circle Intersector)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (com.badlogic.gdx.maps.objects RectangleMapObject)
           (clodiku.components Spatial))
  (:require [brute.entity :as be]
            [clodiku.maps.map-core :as maps]))

(defn intersects?
  "Tests whether or not two shapes intersect"
  [s1 s2]
  (Intersector/overlaps s1 s2))

(defn collides-with-entities?
  "Checks if a given entity collides with another entity on the map"
  [entity-space other-spaces]
  (reduce (fn [collision? circle]
            (or collision? (intersects? entity-space circle))) false other-spaces))

; TODO Generalize this to handle all collisions?
(defn get-entity-collisions
  "Gets a sequence of entities that fall in the collision zone. Only cares about entities with the
  Player or MobAI components at this point."
  [system entity-space other-entities]
 (filter #(intersects? entity-space (:pos (be/get-component system % Spatial))) other-entities))

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
        entity-mov-y (Circle. (.x circle) (+ (.y circle) (:y move)) (.radius circle))
        map-objects (-> ^TiledMap (maps/get-current-map system)
                        (.getLayers)
                        (.get "collision")
                        (.getObjects))
        all-entity-spaces (map #(:pos (be/get-component system % Spatial))
                               (be/get-all-entities-with-component system Spatial))
        other-entity-spaces (remove #(= circle %) all-entity-spaces)
        collide-x? (or (collides-with-map? entity-mov-x map-objects) (collides-with-entities? entity-mov-x other-entity-spaces))
        collide-y? (or (collides-with-map? entity-mov-y map-objects) (collides-with-entities? entity-mov-y other-entity-spaces))
        dx (if collide-x? 0 (:x move))
        dy (if collide-y? 0 (:y move))]
    (Circle.
      (+ (.x circle) dx)
      (+ (.y circle) dy)
      (.radius circle))))
