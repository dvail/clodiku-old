(ns clodiku.util.entities
  (:import (clodiku.components Player Spatial WorldMap State EqWeapon Equipable))
  (:require [brute.entity :as be]
            [clodiku.components :as comps]
            [clojure.set :refer [union]]))

(defn get-attackers
  "Gets a sequence of entities who are currently attacking"
  [system]
  (let [entities (be/get-all-entities-with-component system State)]
    (filter (fn [ent]
              (= (comps/states :melee)
                 (:current (be/get-component system ent State)))) entities)))

(defn get-entity-weapon
  "Gets the equipment piece in the given slot from the entity that owns it"
  [system entity]
  (be/get-component system
                    (:held (:equipment (be/get-component system entity Equipable)))
                    EqWeapon))

(defn get-player-component
  "Get a named component type from the player"
  [system comp]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (be/get-component system player comp)))

; TODO Test this - not sure if it works yet
(defn get-entities-with-components
  "Get entities that have each of a given component list."
  [system & components]
  (reduce #(union %1 (set (be/get-all-entities-with-component system %2))) #{} components))

(defn get-player-pos
  "Get the Spatial component of the player character"
  [system]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (:pos (be/get-component system player Spatial))))

