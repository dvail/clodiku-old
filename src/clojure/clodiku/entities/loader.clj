;;; This is a collection of functions that define mobs available in the game
;;; Each function should return a seqence of data components to be attached to an entity
(ns clodiku.entities.loader
  (:require [clodiku.entities.components :as comps]
            [clodiku.util.rendering]
            [brute.entity :as be]
            [clodiku.entities.util :as eu]
            [clodiku.entities.templates :as et])
  (:import (clodiku.entities.components Equipment EqItem)))

(defn entities->eq
  [system entities]
  "Constructs a map of eq based on a list of entities with EqItem components"
  (reduce #(assoc %1 (:slot (eu/comp-data system %2 EqItem)) %2) {} entities))

(defn make-entity-components
  "Evaluates the default template components and overridden components and merges them into a component set."
  [entity templates]
  (let [entity-type (:template entity)
        entity-comp-map (->> templates entity-type :components)]
    (vals (merge (comps/construct-map entity-comp-map)
                 (comps/construct-map (:components entity))))))

(defn make-mob-equipment
  [mob]
  "Creates a collection of equipment items for a mob"
  (map #(make-entity-components % et/item-templates)
       (-> ((:template mob) et/mob-templates) :equipment vals)))

(defn bind-comps
  "Binds a collection of component lists to the system, returns a map with the new system
  and a list of newly created entitiesb"
  [system comp-lists]
  (reduce (fn [sys-map comp]
            (let [entity (be/create-entity)
                  sys (be/add-entity (:system sys-map) entity)]
              (assoc sys-map :system (reduce #(be/add-component %1 entity %2) sys comp)
                             :entities (conj (:entities sys-map) entity))))
          {:system system :entities '()}
          comp-lists))

(defn bind-mob
  "Creates an entity for a mob and initilizes nested components (inv, eq)"
  [system mob]
  (let [entity (be/create-entity)
        sys-map (bind-comps system (make-mob-equipment mob))
        sys (be/add-entity (:system sys-map) entity)
        eq (entities->eq sys (:entities sys-map))]
    (-> (reduce #(be/add-component %1 entity %2) sys (make-entity-components mob et/mob-templates))
        (eu/comp-update entity Equipment {:items eq}))))

(defmulti init-entities
          "Initialize components and bind data file entities to the system"
          (fn [_ _ type] type))

(defmethod init-entities :mobs
  [system data _]
  (reduce #(bind-mob %1 %2) system (:mobs data)))

(defmethod init-entities :free-items
  [system data _]
  (reduce #(:system (bind-comps %1 (list %2)))
          system
          (map #(make-entity-components % et/item-templates) (:free-items data))))

(defmethod init-entities :default [system _ _] system)

(defn init-area
  "Initializes all entities from a given area data file"
  [system area-data]
  (reduce #(init-entities %1 area-data %2) system (keys area-data)))