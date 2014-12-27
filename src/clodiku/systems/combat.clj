(ns clodiku.systems.combat
  (:require [clodiku.util.entities :as eu]))

(defn update-attack-components
  [system delta]
  (let [attackers (eu/get-attackers system)]
    system))

(defn update
  "Apply combat events and collisions"
  [system delta]
  (-> system
      (update-attack-components delta)))
