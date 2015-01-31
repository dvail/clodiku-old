(ns clodiku.systems.mob-ai
  (:import (clodiku.components MobAI Spatial Player State)
           (clodiku.pathfinding AStar AStar$Node))
  (:require [clodiku.util.entities :as eu]
            [clodiku.maps.map-core :as maps]
            [clodiku.util.movement :as move]
            [clodiku.combat.core :as combat]))

; How often the AI "thinks" and decides to change its behavior
(def ai-speed 4)

; The max distance along the x or y axis that a mob will wander..
(def wander-distance 600)

; The path is a list of tile center points in :x :y form
(defn set-path-to
  "Sets a mob on a path to a given location."
  [system entity goal]
  (let [current-pos (:pos (eu/comp-data system entity Spatial))
        tile-size maps/tile-size
        new-location (AStar$Node. (int (/ (:x goal) tile-size)) (int (/ (:y goal) tile-size)))
        curr-location (AStar$Node. (int (/ (:x current-pos) tile-size)) (int (/ (:y current-pos) tile-size)))
        grid (maps/get-current-map-grid system)
        path (map (fn [^AStar$Node node]
                    {:x (+ (* (.x node) tile-size) (/ tile-size 2))
                     :y (+ (* (.y node) tile-size) (/ tile-size 2))})
                  (AStar/findPath grid curr-location new-location))]
    (eu/comp-update system entity MobAI {:path path})))

(defn pursue-player
  "Move towards the player. Try to straight line approach, otherwise set a path to follow."
  [system delta mob]
  (let [player-pos (eu/get-player-pos system)
        path (:path (eu/comp-data system mob MobAI))]
    (if (empty? path)
      (set-path-to system mob player-pos)
      (move/navigate-path system delta mob))))

(defn attack-player
  "Approach and attempt to attack the player. Right now this is just dumb melee attacks."
  [system delta mob]
  (let [player-pos (eu/get-player-pos system)
        current-pos (:pos (eu/comp-data system mob Spatial))]
    ; TODO Magic attack range
    (if (< (move/dist-between player-pos current-pos) 40)
      (combat/init-attack system delta mob)
      system)))

(defn do-wander
  "Just... wander around."
  [system delta mob]
  (let [path (:path (eu/comp-data system mob MobAI))]
    (if (empty? path)
      system
      (move/navigate-path system delta mob))))

(defn do-aggro
  "Pursue and attack the player, if in range."
  [system delta mob]
  (let [player-pos (:pos (eu/comp-data system (eu/first-entity-with-comp system Player) Spatial))
        current-pos (:pos (eu/comp-data system mob Spatial))]
    ; TODO Magic number on when to pursue player
    (if (< (move/dist-between player-pos current-pos) 300)
      (-> system
          (pursue-player delta mob)
          (attack-player delta mob))
      (do-wander system delta mob))))

(defn random-wander-location
  "Gets a random location for a mob to wander to."
  [system mob]
  (let [current-pos (:pos (eu/comp-data system mob Spatial))]
    {:x (Math/abs (int (+ (- (/ wander-distance 2) (rand wander-distance)) (:x current-pos))))
     :y (Math/abs (int (+ (- (/ wander-distance 2) (rand wander-distance)) (:y current-pos))))}))

(defn update-mob-timestamp
  "Updates the last time the mob had to make a descision"
  [system mob new-delta]
  (eu/comp-update system mob MobAI {:last-update new-delta}))

(defn update-mob-behavior
  "Updates the Mob's choice of behavior"
  [system mob state]
  (if (= state :wander)
    (set-path-to system mob (random-wander-location system mob))
    system))

; Map Mob states to action functions
(def ai-state-actions {:wander do-wander
                       :aggro  do-aggro})

(defn process-ai
  "Process the AI actions for a mob if in a 'free' state."
  [system delta mob]
  (let [ai (eu/comp-data system mob MobAI)
        mob-state (:state ai)
        last-update (:last-update ai)]
    (if (> last-update ai-speed)
      (-> system
          (update-mob-timestamp mob 0)
          (update-mob-behavior mob mob-state)
          ((get ai-state-actions mob-state) delta mob))
      (-> system
          (update-mob-timestamp mob (+ last-update delta))
          ((get ai-state-actions mob-state) delta mob)))))

(defn update
  [system delta]
  (let [mobs (eu/get-entities-with-components system MobAI)]
    (reduce (fn [sys mob]
              (let [main-state (:current (eu/comp-data sys mob State))]
                (cond (= main-state :standing) (process-ai sys delta mob)
                      (= main-state :walking) (process-ai sys delta mob)
                      (= main-state :melee) (combat/advance-attack-state sys delta mob)
                      :else sys))) system mobs)))