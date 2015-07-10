(ns clodiku.systems.events)

(defn update-combat-events
  "Updates information about attacks, etc."
  [delta events]
  (let [evts (:combat @events)
        updated-events (map #(assoc % :delta (+ delta (:delta %))) evts)
        filtered-events (filter #(> 1 (:delta %)) updated-events)]
    (swap! events #(assoc-in %1 [:combat] %2) filtered-events)))

(defn process
  "Process the event queue"
  [system delta events]
  (update-combat-events delta events)
  system)