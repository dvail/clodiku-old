(ns clodiku.systems.events
  (:require [clodiku.entities.util :as eu])
  (:import (clodiku.entities.components Inventory Spatial EqItem Equipment)))

(def ^:const event-categories '(:ui :combat))

(defn- advance-event-times
  "Update the time counter on all events. Events with a total time of > 1 are expired and removed."
  [delta events event-type]
  (->> (event-type @events)
       (map #(assoc % :delta (+ delta (:delta %))))
       (filter #(> 1 (:delta %)))
       (swap! events #(assoc %1 event-type %2))))

(defn- remove-event!
  [events event category]
  (->> (category @events)
       (filter #(not= event %))
       (swap! events #(assoc %1 category %2))))

(defn- unequip-item
  "Removes an equiped item from an entity"
  [system entity slot]
  (let [eq (:items (eu/comp-data system entity Equipment))
        inv (:items (eu/comp-data system entity Inventory))
        old-item (slot eq)]
    (if old-item
      (-> system
          (eu/comp-update entity Equipment {:items (dissoc eq slot)})
          (eu/comp-update entity Inventory {:items (conj inv old-item)}))
      system)))

(defmulti process-event "Process a single event with a given type" (fn [_ _ _ event _] (:type event)))

(defmethod process-event :unequip-item [system delta events event category]
  (remove-event! events event category)
  (unequip-item system (:target event)
                (:slot (eu/comp-data system (:item event) EqItem))))

(defmethod process-event :equip-item [system delta events event category]
  (let [target (:target event)
        item (:item event)
        slot (:slot (eu/comp-data system item EqItem))
        new-system (unequip-item system target slot)
        new-inventory (filter #(not= item %)
                              (:items (eu/comp-data new-system target Inventory)))
        target-eq (:items (eu/comp-data new-system target Equipment))]
    (remove-event! events event category)
    (-> new-system
        (eu/comp-update target Inventory {:items new-inventory})
        (eu/comp-update target Equipment {:items (assoc target-eq slot item)}))))

(defmethod process-event :drop-item [system _ events event category]
  (let [target (:target event)
        item (:item event)
        target-pos (:pos (eu/comp-data system target Spatial))
        new-inventory (filter #(not= item %)
                              (:items (eu/comp-data system target Inventory)))]
    (remove-event! events event category)
    (-> system
        (eu/comp-update target Inventory {:items new-inventory})
        (eu/comp-update item Spatial {:pos target-pos}))))

(defmethod process-event :default [system _ _ event _]
  (println (:type event))
  system)

; TODO Limit the number of events taken per cycle here???
(defmulti process-event-list "Apply any effects of game events." (fn [_ _ _ event-category] event-category))

(defmethod process-event-list :combat [system delta events event-category]
  (advance-event-times delta events event-category)
  system)

(defmethod process-event-list :ui [system delta events event-category]
  (reduce #(process-event %1 delta events %2 :ui) system (:ui @events)))

(defn process
  "Process the event queue"
  [system delta events]
  (reduce #(process-event-list %1 delta events %2) system event-categories))