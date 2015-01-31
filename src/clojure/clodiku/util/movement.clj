(ns clodiku.util.movement
  (:import (com.badlogic.gdx.math Circle Intersector)
           (com.badlogic.gdx.maps.objects RectangleMapObject)
           (clodiku.components Spatial State))
  (:require [clodiku.maps.map-core :as maps]
            [clodiku.util.entities :as eu]
            [clodiku.components :as comps]))

(defn intersects?
  "Tests whether or not two shapes intersect"
  [s1 s2]
  (Intersector/overlaps s1 s2))

(defn spatial-as-circle
  "Gets a Circle object representation of an entities size and position"
  [spatial-comp]
  (Circle. (:x (:pos spatial-comp)) (:y (:pos spatial-comp)) (:size spatial-comp)))

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
  (filter #(intersects? entity-space (spatial-as-circle (eu/comp-data system % Spatial))) other-entities))

(defn collides-with-map?
  "Tests if the entity will collide with an impassable area of the map"
  [entity-space map-objs]
  (reduce (fn [collision? object]
            (or collision? (intersects? entity-space (.getRectangle ^RectangleMapObject object)))) false map-objs))

(defn get-movement-map
  "Given an attempted movement, check the map and other entities for collisions and
  return an x/y map representing the actual movement taken"
  [system spatial move]
  (let [pos (:pos spatial)
        entity-mov-x (Circle. (+ (:x pos) (:x move)) (:y pos) (:size spatial))
        entity-mov-y (Circle. (:x pos) (+ (:y pos) (:y move)) (:size spatial))
        map-objects (maps/get-map-obstacles system)
        all-entity-circles (map #(spatial-as-circle (eu/comp-data system % Spatial))
                                (eu/get-entities-with-components system Spatial))
        other-entity-spaces (remove #(= (spatial-as-circle spatial) %) all-entity-circles)
        collide-x? (or (collides-with-map? entity-mov-x map-objects) (collides-with-entities? entity-mov-x other-entity-spaces))
        collide-y? (or (collides-with-map? entity-mov-y map-objects) (collides-with-entities? entity-mov-y other-entity-spaces))
        dx (if collide-x? 0 (:x move))
        dy (if collide-y? 0 (:y move))]
    {:x (+ (:x pos) dx)
     :y (+ (:y pos) dy)}))

(defn vector->direction
  "Returns the compass direction based on a non-zero movement vector"
  [x y]
  (cond
    (< 0 x) (comps/directions :east)
    (> 0 x) (comps/directions :west)
    (< 0 y) (comps/directions :north)
    :else (comps/directions :south)))

(defn move-entity
  [system delta entity {:keys [x y]}]
  (let [spatial (eu/comp-data system entity Spatial)
        state (eu/comp-data system entity State)
        newstate (if (= x y 0)
                   (comps/states :standing)
                   (comps/states :walking))
        newdelta (if (= newstate (:current state)) (+ delta (:time state)) 0)
        newdirection (if (= x y 0)
                       (:direction spatial)
                       (vector->direction x y))]
    (-> system
        (eu/comp-update entity Spatial {:pos       (get-movement-map system spatial {:x x :y y})
                                        :direction newdirection})
        (eu/comp-update entity State {:current newstate
                                      :time    newdelta}))))
