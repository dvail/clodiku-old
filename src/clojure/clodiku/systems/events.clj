(ns clodiku.systems.events
  (:require [clodiku.entities.util :as eu])
  (:import (clodiku.entities.components Inventory Spatial)))

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

(defmulti process-event "Process a single events with a given type" (fn [_ _ _ event _] (:type event)))

(defmethod process-event :equip-item [system delta events event category]
  system)

(defmethod process-event :drop-item [system delta events event category]
  (let [target (:target event)
        item (:item event)
        target-pos (:pos (eu/comp-data system target Spatial))
        inventory (eu/comp-data system target Inventory)
        new-inventory (filter #(not= item %) (:items inventory))]
    (remove-event! events event category)
    (-> system
        (eu/comp-update target Inventory {:items new-inventory})
        (eu/comp-update item Spatial {:pos target-pos}))))

(defmethod process-event :default [system _ _ _ _]
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