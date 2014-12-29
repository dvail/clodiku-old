(ns clodiku.systems.combat
  (:import (clodiku.components Spatial EqWeapon Equipable)
           (com.badlogic.gdx.math Rectangle Circle))
  (:require [clodiku.util.entities :as eu]
            [brute.entity :as be]
            [clodiku.components :as comps]))

(defn update-attack-components
  [system delta attacker]
  (let [weapon-entity (:held (:equipment (be/get-component system attacker Equipable)))
        weapon-comp (be/get-component system weapon-entity EqWeapon)
        entity-space (be/get-component system attacker Spatial)
        entity-facing (:direction entity-space)
        new-hit-box ((:hit-box-fn weapon-comp) (:hit-box weapon-comp) entity-facing delta)]
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
