(ns clodiku.systems.events
  (:require [clodiku.entities.util :as eu])
  (:import (clodiku.entities.components Inventory Spatial EqItem Equipment)))

(def ^:const event-categories '(:ui :combat :animation))

(def events (atom {:ui '()
                   :combat '()
                   :animation '()}))

(defn- advance-event-times
  "Update the time counter on all events. Events with a total time of > 1 are expired and removed."
  [delta event-type]
  (->> (event-type @events)
       (map #(assoc % :delta (+ delta (:delta %))))
       (filter #(> 1 (:delta %)))
       (swap! events #(assoc %1 event-type %2))))

(defn- remove-event!
  [event category]
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

(defn add-event
  [category event]
  (swap! events #(assoc %1 category %2)
         (conj (category @events) event)))

(defn get-events
  ([] @events)
  ([category] (category @events)))

(defmulti process-event "Process a single event with a given type" (fn [_ _ event _] (:type event)))

(defmethod process-event :unequip-item [system delta event category]
  (remove-event! event category)
  (unequip-item system (:target event)
                (:slot (eu/comp-data system (:item event) EqItem))))

(defmethod process-event :equip-item [system delta event category]
  (let [target (:target event)
        item (:item event)
        slot (:slot (eu/comp-data system item EqItem))
        new-system (unequip-item system target slot)
        new-inventory (filter #(not= item %)
                              (:items (eu/comp-data new-system target Inventory)))
        target-eq (:items (eu/comp-data new-system target Equipment))]
    (remove-event! event category)
    (-> new-system
        (eu/comp-update target Inventory {:items new-inventory})
        (eu/comp-update target Equipment {:items (assoc target-eq slot item)}))))

(defmethod process-event :drop-item [system _ event category]
  (let [target (:target event)
        item (:item event)
        target-pos (:pos (eu/comp-data system target Spatial))
        new-inventory (filter #(not= item %)
                              (:items (eu/comp-data system target Inventory)))]
    (remove-event! event category)
    (-> system
        (eu/comp-update target Inventory {:items new-inventory})
        (eu/comp-update item Spatial {:pos target-pos}))))

(defmethod process-event :state-change [system _ event category]
  (println event)
  (remove-event! event category)
  system)

(defmethod process-event :default [system _ event _]
  system)

; TODO Limit the number of events taken per cycle here???
(defmulti process-event-list "Apply any effects of game events." (fn [_ _ event-category] event-category))

(defmethod process-event-list :combat [system delta event-category]
  (advance-event-times delta event-category)
  system)

; TODO Generalize these better
(defmethod process-event-list :ui [system delta event-category]
  (reduce #(process-event %1 delta %2 :ui) system (:ui @events)))

(defmethod process-event-list :animation [system delta event-category]
  (reduce #(process-event %1 delta %2 :animation) system (:animation @events)))

(defn process
  "Process the event queue"
  [system delta]
  (reduce #(process-event-list %1 delta %2) system event-categories))