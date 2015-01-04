(ns clodiku.systems.combat
  (:import (clodiku.components Spatial EqWeapon Equipable))
  (:require [clodiku.util.entities :as eu]
            [brute.entity :as be]
            [clodiku.equipment.weaponry :as weaponry]))

(defn update-attack-components
  [system delta attacker]
  (let [weapon-entity (:held (:equipment (be/get-component system attacker Equipable)))
        weapon-comp (be/get-component system weapon-entity EqWeapon)
        entity-space (be/get-component system attacker Spatial)
        new-hit-box ((weaponry/get-attack-fn (:type weapon-comp)) (:hit-box weapon-comp) entity-space)]
    (-> system
        (be/update-component weapon-entity EqWeapon
                             (fn [weap]
                               (assoc weap :hit-box new-hit-box))))))

(defn update
  "Apply combat events and collisions"
  [system delta]
  (reduce
    (fn [sys attacker]
      (-> sys
          (update-attack-components delta attacker))) system (eu/get-attackers system)))
