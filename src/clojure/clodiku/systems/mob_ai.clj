(ns clodiku.systems.mob-ai
  (:import (clodiku.components MobAI Spatial)
           (clodiku.pathfinding AStar AStar$Node))
  (:require [brute.entity :as be]
            [clodiku.util.entities :as eu]
            [clodiku.maps.map-core :as maps]))

; How often the AI "thinks" and decides to change its behavior
(def ai-speed 3)

; The max distance along the x or y axis that a mob will wander..
(def wander-distance 400)

(defn do-wander
  "Just... wander around."
  [system delta mob]
  system)

(defn do-aggro
  "Pursue and attack the player, if in range."
  [system delta mob]
  system)

(defn set-path-to-location
  "Get a path to the next target location, the path should be a vector of
  x/y coordinates (Vector2) that the mob attempts to move to."
  [system mob]
  (let [current-pos (:pos (be/get-component system mob Spatial))
        tile-size maps/tile-size
        new-x (int (+ (- (/ wander-distance 2) (rand wander-distance)) (.x current-pos)))
        new-y (int (+ (- (/ wander-distance 2) (rand wander-distance)) (.y current-pos)))
        curr-location (AStar$Node. (int (/ (.x current-pos) tile-size)) (int (/ (.y current-pos) tile-size)))
        new-location (AStar$Node. (int (/ new-x tile-size)) (int (/ new-y tile-size)))
        grid (maps/get-current-map-grid system)
        path (AStar/findPath grid curr-location new-location)]
    (println [(.x curr-location) (.y curr-location)])
    (println (map (fn [node] [(.x node) (.y node)]) (vec path)))
    system))

(defn update-mob-timestamp
  "Updates the last time the mob had to make a descision"
  [system mob new-delta]
  (let [ai-component (be/get-component system mob MobAI)
        new-data (assoc (:data ai-component) :last-update new-delta)]
    (be/update-component system mob MobAI (fn [ai]
                                            (assoc ai :data new-data)))))

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
  (let [mobs (be/get-all-entities-with-component system MobAI)]
    (reduce (fn [sys mob]
              (let [ai-component (be/get-component sys mob MobAI)
                    mob-state (:state ai-component)
                    last-update (:last-update (:data ai-component))]
                (if (> last-update ai-speed)
                  (-> sys
                      (update-mob-timestamp mob 0)
                      (update-mob-behavior mob mob-state)
                      ((get state-actions mob-state) delta mob))
                  (-> sys
                      (update-mob-timestamp mob (+ last-update delta))
                      ((get state-actions mob-state) delta mob))))) system mobs)))