(ns clodiku.systems.mob-ai
  (:import (clodiku.components MobAI Spatial State)
           (clodiku.pathfinding AStar AStar$Node))
  (:require [clodiku.util.entities :as eu]
            [clodiku.maps.map-core :as maps]
            [clodiku.components :as comps]
            [clodiku.util.movement :as move]))

; How often the AI "thinks" and decides to change its behavior
(def ai-speed 4)

; The max distance along the x or y axis that a mob will wander..
(def wander-distance 600)

(defn move-mob
  "Move an entity towards a position"
  [system delta entity move-pos]
  (let [pos (:pos (eu/comp-data system entity Spatial))
        delta-x (Math/abs ^float (- (:x move-pos) (:x pos)))
        delta-y (Math/abs ^float (- (:y move-pos) (:y pos)))
        ; TODO replace magic number here with movement speed
        move {:x (if (> (:x move-pos) (:x pos))
                   (min delta-x 1)
                   (* -1 (min delta-x 1)))
              :y (if (> (:y move-pos) (:y pos))
                   (min delta-y 1)
                   (* -1 (min delta-y 1)))}]
    (move/move-entity system delta entity move)))

(defn do-wander
  "Just... wander around."
  [system delta mob]
  (let [current-pos (:pos (eu/comp-data system mob Spatial))
        path (:path (eu/comp-data system mob MobAI))
        move-pos (last path)]
    (if (nil? move-pos)
      system
      (if (and (> 2 (Math/abs ^float (- (:x current-pos) (:x move-pos))))
               (> 2 (Math/abs ^float (- (:y current-pos) (:y move-pos)))))
        (eu/comp-update system mob MobAI {:path (drop-last path)})
        (move-mob system delta mob move-pos)))))

(defn do-aggro
  "Pursue and attack the player, if in range."
  [system delta mob]
  system)

(defn set-path-to-location
  "Get a path to the next target location, the path should be a vector of
  x/y coordinates (Vector2) that the mob attempts to move to."
  [system mob]
  (let [current-pos (:pos (eu/comp-data system mob Spatial))
        tile-size maps/tile-size
        new-x (Math/abs (int (+ (- (/ wander-distance 2) (rand wander-distance)) (:x current-pos))))
        new-y (Math/abs (int (+ (- (/ wander-distance 2) (rand wander-distance)) (:y current-pos))))
        curr-location (AStar$Node. (int (/ (:x current-pos) tile-size)) (int (/ (:y current-pos) tile-size)))
        new-location (AStar$Node. (int (/ new-x tile-size)) (int (/ new-y tile-size)))
        grid (maps/get-current-map-grid system)
        ; The path is a list of tile center points in :x :y form
        path (map (fn [^AStar$Node node]
                    {:x (+ (* (.x node) maps/tile-size) (/ maps/tile-size 2))
                     :y (+ (* (.y node) maps/tile-size) (/ maps/tile-size 2))})
                  (AStar/findPath grid curr-location new-location))]
    (eu/comp-update system mob MobAI {:path path})))

(defn update-mob-timestamp
  "Updates the last time the mob had to make a descision"
  [system mob new-delta]
  (eu/comp-update system mob MobAI {:last-update new-delta}))

(defn update-mob-behavior
  "Updates the Mob's choice of behavior"
  [system mob state]
  (if (= state :wander)
    (set-path-to-location system mob)
    system))

; Map Mob states to action functions
(def state-actions {:wander do-wander
                    :aggro  do-aggro})

(defn update
  [system delta]
  (let [mobs (eu/get-entities-with-components system MobAI)]
    (reduce (fn [sys mob]
              (let [ai (eu/comp-data sys mob MobAI)
                    mob-state (:state ai)
                    last-update (:last-update ai)]
                (if (> last-update ai-speed)
                  (-> sys
                      (update-mob-timestamp mob 0)
                      (update-mob-behavior mob mob-state)
                      ((get state-actions mob-state) delta mob))
                  (-> sys
                      (update-mob-timestamp mob (+ last-update delta))
                      ((get state-actions mob-state) delta mob))))) system mobs)))