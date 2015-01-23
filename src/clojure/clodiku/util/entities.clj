;;;; This module is a collection of functions to quickly access/update the
;;;; ECS data store. Ideally this will be a wrapper over whatever Entity System used so that
;;;; it may be more feasible to switch out in the future e.g. for performance reasons.

(ns clodiku.util.entities
  (:import (clodiku.components Player Spatial State EqWeapon Equipable))
  (:require [brute.entity :as be]
            [clodiku.components :as comps]
            [clojure.set :refer [union]]))

;;;
;;; General functions to access/update arbitrary entities.
;;;

(defn first-entity-with-comp
  "Gets the first entity with the given component. Useful for 'Singleton' entities,
  such as the player or the current world map."
  [system type]
  (first (be/get-all-entities-with-component system type)))

(defn comp-data
  "Get the data map of a given component."
  [system entity type]
  (:data (be/get-component system entity type)))

(defn comp-update
  "Update the value of a component"
  [system entity type data-map]
  (let [component (be/get-component system entity type)
        new-data (merge (:data component) data-map)]
    (be/update-component system entity type (fn [comp-data]
                                              (assoc comp-data :data new-data)))))

(defn get-attackers
  "Gets a sequence of entities who are currently attacking"
  [system]
  (let [entities (be/get-all-entities-with-component system State)]
    (filter (fn [ent]
              (= (comps/states :melee)
                 (:current (comp-data system ent State)))) entities)))

(defn get-entity-weapon
  "Gets the equipment piece in the given slot from the entity that owns it"
  [system entity]
  (comp-data system
             (:held (:equipment (comp-data system entity Equipable)))
             EqWeapon))

(defn get-player-component
  "Get a named component type from the player"
  [system comp]
  (let [player (first-entity-with-comp system Player)]
    (comp-data system player comp)))

; TODO Test this - not sure if it works yet
(defn get-entities-with-components
  "Get entities that have each of a given component list."
  [system & components]
  (reduce #(union %1 (set (be/get-all-entities-with-component system %2))) #{} components))

(defn get-player-pos
  "Get the Spatial component of the player character"
  [system]
  (let [player (first-entity-with-comp system Player)]
    (:pos (comp-data system player Spatial))))

