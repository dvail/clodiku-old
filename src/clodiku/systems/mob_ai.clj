(ns clodiku.systems.mob-ai
  (:require [brute.entity :as be]
            [clodiku.components :as comps]
            [clodiku.util.collision :as coll]
            [clodiku.util.entities :as eu])
  (:import (clodiku.components MobAI)))

; How often the AI "thinks" and decides to change its behavior
(defn ai-speed 4)

(defn do-wander
  "Just... wander around."
  [system delta mob]
  (println mob)
  system)

(defn do-aggro
  "Pursue and attack the player, if in range."
  [system delta mob]
  system)

(defn update-mob
  "Updates the Mob's choice of behavior"
  [system delta mob]
  system)

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
                  (-> system
                      (update-mob delta mob)
                      ((get state-actions mob-state) delta mob))
                  ((get state-actions mob-state) system delta mob)))) system mobs)))
