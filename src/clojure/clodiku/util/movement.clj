(ns clodiku.util.movement
  (:import (com.badlogic.gdx.math Circle Intersector Rectangle)
           (com.badlogic.gdx.maps.objects RectangleMapObject)
           (clodiku.entities.components Spatial State MobAI Inventory))
  (:require [clodiku.systems.events :as events]
    [clodiku.world.maps :as maps]
            [clodiku.world.transporter :as transport]
            [clodiku.entities.util :as eu]
            [clodiku.entities.components :as comps]))

(defn spatial-as-circle
  "Gets a Circle object representation of an entities size and position"
  [spatial-comp]
  (Circle. (:x (:pos spatial-comp)) (:y (:pos spatial-comp)) (:size spatial-comp)))

(defmulti intersects? "Tests whether or not two shapes intersect"
          (fn [s1 s2] [(class s1) (class s2)]))

(defmethod intersects? [Circle Circle] [s1 s2]
  (Intersector/overlaps ^Circle s1 ^Circle s2))

(defmethod intersects? [Circle Rectangle] [s1 s2]
  (Intersector/overlaps ^Circle s1 ^Rectangle s2))

(defmethod intersects? [Spatial Spatial] [s1 s2]
  (Intersector/overlaps ^Circle (spatial-as-circle s1) ^Circle (spatial-as-circle s2)))

; TODO Implement this later to save on pathfinding?
(defn clear-line-of-sight?
  "Tests whether or not there is a direct path between the two points without a collision on the map."
  [system pos-a pos-b]
  system)

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

(defn try-transport
  "If the player hits a transport zone, begin the process to swap out world and other entities."
  [system player move]
  (let [spatial (eu/comp-data system player Spatial)
        pos (:pos spatial)
        map-transports (maps/get-map-transports system)
        entity-mov-x (Circle. (+ (:x pos) (:x move)) (:y pos) (:size spatial))
        entity-mov-y (Circle. (:x pos) (+ (:y pos) (:y move)) (:size spatial))
        collide-x? (collides-with-map? entity-mov-x map-transports)
        collide-y? (collides-with-map? entity-mov-y map-transports)]
    (if (or collide-x? collide-y?)
      (transport/swap-areas system (first map-transports))
      system)))

(defn get-movement-map
  "Given an attempted movement, check the map and other entities for collisions and
  return an x/y map representing the actual movement taken"
  [system spatial move]
  (let [pos (:pos spatial)
        entity-mov-x (Circle. (+ (:x pos) (:x move)) (:y pos) (:size spatial))
        entity-mov-y (Circle. (:x pos) (+ (:y pos) (:y move)) (:size spatial))
        map-objects (maps/get-map-obstacles system)
        all-entity-circles (map #(spatial-as-circle (eu/comp-data system % Spatial))
                                (eu/collision-entities system))
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
    (< 0 x) :east
    (> 0 x) :west
    (< 0 y) :north
    :else :south))

(defn dist-between
  "The distance between two points"
  [pos-a pos-b]
  (+ (Math/abs (float (- (:x pos-a) (:x pos-b))))
     (Math/abs (float (- (:y pos-a) (:y pos-b))))))

(defn update-state
  [system delta entity {:keys [x y]}]
  (let [state (eu/comp-data system entity State)
        newstate (if (= x y 0)
                   (comps/states :standing)
                   (comps/states :walking))
        state-changed? (= newstate (:current state))
        newdelta (if (not state-changed?) (+ delta (:time state)) 0)]
    (when state-changed?
      (events/add-event :animation {:type      :state-change
                                    :entity    entity
                                    :new-state newstate}))
    (-> system
        (eu/comp-update entity State {:current newstate
                                      :time    newdelta}))))

(defn update-position
  [system _ entity {:keys [x y]}]
  (let [spatial (eu/comp-data system entity Spatial)
        newdirection (if (= x y 0)
                       (:direction spatial)
                       (vector->direction x y))]
    (eu/comp-update system entity Spatial {:pos       (get-movement-map system spatial {:x x :y y})
                                           :direction newdirection})))

(defn move-mob
  "Move an entity towards a position"
  [system delta entity move-pos]
  (let [pos (:pos (eu/comp-data system entity Spatial))
        delta-x (Math/abs ^float (- (:x move-pos) (:x pos)))
        delta-y (Math/abs ^float (- (:y move-pos) (:y pos)))
        ; TODO replace magic number here with movement speed
        move {:x (if (> (:x move-pos) (:x pos))
                   (min delta-x 2)
                   (* -1 (min delta-x 2)))
              :y (if (> (:y move-pos) (:y pos))
                   (min delta-y 2)
                   (* -1 (min delta-y 2)))}]
    (-> system
        (update-position delta entity move)
        (update-state delta entity move))))

(defn navigate-path
  "Move an entity along a prediscovered path."
  [system delta mob]
  (let [current-pos (:pos (eu/comp-data system mob Spatial))
        path (:path (eu/comp-data system mob MobAI))
        move-pos (last path)]
    (if (and (> 2 (Math/abs ^float (- (:x current-pos) (:x move-pos))))
             (> 2 (Math/abs ^float (- (:y current-pos) (:y move-pos)))))
      (eu/comp-update system mob MobAI {:path (drop-last path)})
      (move-mob system delta mob move-pos))))

(defn grab-item
  "Add a free item to an entity's inventory if the entity and item intersect."
  [system entity]
  (let [target-item (first (filter #(intersects?
                                     (eu/comp-data system entity Spatial)
                                     (eu/comp-data system % Spatial)) (eu/free-items system)))]
    (if target-item
      (-> system
          (eu/comp-update entity Inventory {:items (conj (:items (eu/comp-data system entity Inventory)) target-item)})
          (eu/comp-update target-item Spatial {:pos :carry}))
      system)))
