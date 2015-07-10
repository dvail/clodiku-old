(ns clodiku.ui.util)

(defn add-event
  "Adds a UI event to the system"
  [events event]
  (swap! events #(assoc %1 :ui (conj (:ui %1) %2)) event))
