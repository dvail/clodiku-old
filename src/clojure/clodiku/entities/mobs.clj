;;; This is a collection of functions that define mobs available in the game
;;; Each function should return a seqence of data components to be attached to an entity
(ns clodiku.entities.mobs
  (:require [clodiku.entities.components :as comps]
            [clodiku.util.rendering]
            [brute.entity :as be]
            [clodiku.entities.util :as eu]
            [clodiku.entities.weapons :as ew])
  (:import (clodiku.entities.components Equipment)))

(def templates {:orc {:components {:state               {:current :walking
                                                         :time    0}
                                   :attribute           {:hp  30 :mp 5 :mv 50
                                                         :str 14 :dex 8 :vit 14 :psy 3}
                                   :spatial             {:pos       {:x 400 :y 400}
                                                         :size      14
                                                         :direction :west}
                                   :equipment           {:items {}}
                                   :animated-renderable {:regions "./assets/mob/orc/orc.pack"}
                                   :mob-ai              {:last-update 0
                                                         :state       :wander}}
                      :inventory  '()
                      :equipment  {:held :sword}}})

(defn merge-default-components
  "Evaluates the default template components and overridden components and merges them into a component set."
  [mob]
  (let [mob-type (:template mob)
        mob-comp-map (->> templates mob-type :components)]
    (merge (comps/construct-map mob-comp-map)
           (comps/construct-map (:components mob)))))

(defn make-mob
  [mob]
  (reduce-kv #(assoc %1 %2 %3) {} (merge-default-components mob)))

(defn make-equipment
  "Construct the mob's equipment set"
  [mob]
  (let [eq (merge (:equipment ((:template mob) templates)) (:equipment mob))]
    (reduce-kv #(assoc %1 %2 (ew/init-item-comps %3)) {} eq)))

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
  (let [eq-comps (make-equipment mob)
        mob-comps (make-mob mob)]
    (bind-to-system system eq-comps mob-comps)))