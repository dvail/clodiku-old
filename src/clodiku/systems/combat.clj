(ns clodiku.systems.combat
  (:import (clodiku.components Spatial EqWeapon Equipable Player MobAI))
  (:require [clodiku.util.entities :as eu]
            [brute.entity :as be]
            [clodiku.equipment.weaponry :as weaponry]
            [clodiku.util.collision :as coll]))

(defn get-defenders
  "Gets all possible entities that could be hit by an attack, excluding the
  initiator of the attack"
  [system attacker]
  (let [defenders (eu/get-entities-with-components system Player MobAI)]
    (clojure.set/difference (set defenders) #{attacker})))

(defn process-attack
  "Dispatch events attack and apply damage to affected entities."
  [system attacker weapon hit-list]
  ; TODO Apply damage, etc. here
  ; TODO Map this over all entities in hit-list
  (let [damage 5
        event {:type     :melee_attack
               :attacker attacker
               :defender (first hit-list)
               :location (:pos (be/get-component system (first hit-list) Spatial))
               :damage   damage
               :delta    0}
        combat-events (:combat (:world_events system))
        new-event-list (conj combat-events event)]
    (println new-event-list)
    (-> system
        (be/update-component weapon EqWeapon
                             (fn [weap]
                               (assoc weap :hit-list hit-list)))
        (assoc-in [:world_events :combat] new-event-list))))

(defn check-attack-collisions
  "Tests is any entities are hit by a weapon. Does not allow an entity
  to hit him/herself when attacking"
  [system attacker weapon]
  (let [weapon-comp (be/get-component system weapon EqWeapon)
        defenders (get-defenders system attacker)
        old-hit-list (:hit-list weapon-comp)
        entities-hit (coll/get-entity-collisions system (:hit-box weapon-comp) defenders)
        new-hit-list (into '() (clojure.set/difference (set entities-hit) (set old-hit-list)))]
    (if (not (empty? new-hit-list))
      (process-attack system attacker weapon new-hit-list)
      system)))

(defn update-entity-attacks
  "Updates the positions of the all weapon hit boxes belonging to attacking entities."
  [system attacker weapon]
  (let [weapon-comp (be/get-component system weapon EqWeapon)
        entity-space (be/get-component system attacker Spatial)
        new-hit-box ((weaponry/get-attack-fn (:type weapon-comp)) (:hit-box weapon-comp) entity-space)]
    (-> system
        (be/update-component weapon EqWeapon
                             (fn [weap]
                               (assoc weap :hit-box new-hit-box))))))

(defn update-combat-events
  "Updates information about attacks, etc."
  [system delta]
  (let [events (:combat (:world_events system))
        updated-events (map #(assoc % :delta (+ delta (:delta %))) events)]
    (assoc-in system [:world_events :combat] updated-events)))

(defn update
  "Apply combat events and collisions"
  [system delta]
  ; TODO This doesn't feel right
  (let [updated-system (update-combat-events system delta)]
    (reduce
      (fn [sys attacker]
        (let [weapon-entity (:held (:equipment (be/get-component system attacker Equipable)))]
          (-> sys
              (update-entity-attacks attacker weapon-entity)
              (check-attack-collisions attacker weapon-entity)))) updated-system (eu/get-attackers updated-system))))
