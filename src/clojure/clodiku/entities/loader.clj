;;; This is a collection of functions that define mobs available in the game
;;; Each function should return a seqence of data components to be attached to an entity
(ns clodiku.entities.loader
  (:require [clodiku.entities.components :as comps]
            [clodiku.util.rendering]
            [brute.entity :as be]
            [clodiku.entities.util :as eu]
            [clodiku.entities.templates :as et])
  (:import (clodiku.entities.components Equipment)))

(defn merge-default-components
  "Evaluates the default template components and overridden components and merges them into a component set."
  [item templates]
  (let [item-type (:template item)
        item-comp-map (->> templates item-type :components)]
    (merge (comps/construct-map item-comp-map)
           (comps/construct-map (:components item)))))

(defn init-item-comps
  "Get a sequence of components based on an item keyword"
  [item]
  (if (keyword? item)
    (map #(apply comps/construct %) (:components (item et/item-templates)))
    (vals (merge-default-components item et/item-templates))))

(defn bind-item
  "Binds a list of components to an entity and adds it to the system"
  [system item-comps]
  (let [item (be/create-entity)]
    (reduce #(be/add-component %1 item %2) system item-comps)))

(defn make-mob
  [mob]
  (reduce-kv #(assoc %1 %2 %3) {} (merge-default-components mob et/mob-templates)))

(defn make-mob-equipment
  "Construct the mob's equipment set"
  [mob]
  (let [eq (merge (:equipment ((:template mob) et/mob-templates)) (:equipment mob))]
    (reduce-kv #(assoc %1 %2 (init-item-comps %3)) {} eq)))

(defn bind-eq
  "Create entities for eq components and add to the system.
  Must return a map with the entity IDs and the updated system"
  [system eq-comps]
  (reduce-kv (fn [sys-map key val]
               (let [entity (be/create-entity)
                     sys (be/add-entity (:system sys-map) entity)]
                 (assoc sys-map :system (reduce #(be/add-component %1 entity %2) sys val)
                                :eq (assoc (:eq sys-map) key entity))))
             {:system system :eq {}}
             eq-comps))

(defn bind-inv
  "Creates general inventory entities and adds them to the system. Then returns a map with the entity
  IDs, the eq IDs, and the updated system"
  [sys-map inv-comps])

(defn bind-mob
  "Receives the system map with eq and inventoty entities and binds the mob components to the system
  and the eq/inventory entities to the corresponding mob components."
  [sys-map mob-comps]
  (let [entity (be/create-entity)
        sys (be/add-entity (:system sys-map) entity)]
    (eu/comp-update (reduce #(be/add-component %1 entity %2) sys (vals mob-comps))
                    entity
                    Equipment
                    {:items (:eq sys-map)})))

(defn bind-to-system
  [system eq-comps mob-comps]
  (-> system
      (bind-eq eq-comps)
      (bind-mob mob-comps)))

(defn init-mob
  "Get a sequence of components based on a mob keyword"
  [system mob]
  (bind-to-system system (make-mob-equipment mob) (make-mob mob)))