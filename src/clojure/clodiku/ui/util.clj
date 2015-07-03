(ns clodiku.ui.util)

(defn add-event
  "Adds a UI event to the system"
  [events event]
  (swap! events (fn [evts evt]
                  (let [ui-evts (:ui-events evts)]
                    (assoc evts :ui-events (conj ui-evts evt)))) event))
