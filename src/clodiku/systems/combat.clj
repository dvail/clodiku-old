(ns clodiku.systems.combat
  (:import (clodiku.components Spatial EqWeapon Equipable Player MobAI))
  (:require [clodiku.util.entities :as eu]
            [brute.entity :as be]
            [clodiku.equipment.weaponry :as weaponry]
            [clodiku.util.collision :as coll]))

(defn get-weapon-component
  "Gets the EqWeapon component from a given attacker"
  [system attacker]
  (let [weapon-entity (:held (:equipment (be/get-component system attacker Equipable)))]
    (be/get-component system weapon-entity EqWeapon)))

(defn get-defenders
  "Gets all possible entities that could be hit by an attack, excluding the
  initiator of the attack"
  [system attacker]
  (let [defenders (eu/get-entities-with-components system Player MobAI)]
    (clojure.set/difference (set defenders) #{attacker})))

(defn check-attack-collisions
  "Tests is any entities are hit by a weapon. Does not allow an entity
  to hit him/herself when attacking"
  [system delta attacker]
  (let [weapon-entity (:held (:equipment (be/get-component system attacker Equipable)))
        weapon-comp (be/get-component system weapon-entity EqWeapon)
        defenders (get-defenders system attacker)
        old-hit-list (:hit-list weapon-comp)
        entities-hit (coll/get-entity-collisions system (:hit-box weapon-comp) defenders)
        new-hit-list (clojure.set/difference (set entities-hit) (set old-hit-list))]
    (if (empty? new-hit-list)
      system
      (-> system
          (be/update-component weapon-entity EqWeapon
                               (fn [weap]
                                 (assoc weap :hit-list new-hit-list)))))))

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

(defn update-combat-events
  "Updates information about attacks, etc."
  [system delta]
  system)

(defn update
  "Apply combat events and collisions"
  [system delta]
  (reduce
    (fn [sys attacker]
      (-> sys
          (update-combat-events delta)
          (update-attack-components delta attacker)
          (check-attack-collisions delta attacker))) system (eu/get-attackers system)))
