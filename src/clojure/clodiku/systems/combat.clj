(ns clodiku.systems.combat
  (:import (clodiku.components Spatial EqWeapon Equipable Player MobAI))
  (:require [clodiku.util.entities :as eu]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.util.movement :as coll]
            [clodiku.components :as comps]
            [clodiku.combat.calulations :as ccalc]))

(defn aggrivate
  "Changes the behavior of all mobs sent as input to aggressive."
  [system mobs]
  (reduce (fn [sys mob]
            (eu/comp-update sys mob MobAI {:state (comps/mob-ai-states :aggro)})) system mobs))

(defn get-defenders
  "Gets all possible entities that could be hit by an attack, excluding the
  initiator of the attack"
  [system attacker]
  (let [defenders (eu/get-entities-with-components system Player MobAI)
        filter-type (if (eu/has-comp? system attacker Player)
                      Player
                      MobAI)]
    (clojure.set/difference (set defenders) (set (eu/get-entities-with-components system filter-type)))))

(defn process-attack
  "Dispatch events attack and apply damage to affected entities."
  [system attacker weapon hit-list]
  ; TODO Apply damage, etc. here
  (reduce (fn [sys hit-entity]
            (let [damage (ccalc/attack-damage sys attacker hit-entity)
                  event {:type     :melee
                         :attacker attacker
                         :defender hit-entity
                         :location (:pos (eu/comp-data sys hit-entity Spatial))
                         :damage   damage
                         :delta    0}
                  combat-events (:combat (:world_events sys))
                  new-event-list (conj combat-events event)
                  old-hit-list (:hit-list (eu/comp-data system weapon EqWeapon))]
              (println damage)
              (-> sys
                  (eu/comp-update weapon EqWeapon {:hit-list (conj old-hit-list hit-entity)})
                  (assoc-in [:world_events :combat] new-event-list)))) system hit-list))

(defn check-attack-collisions
  "Tests is any entities are hit by a weapon. Does not allow an entity
  to hit him/herself when attacking"
  [system attacker weapon]
  (let [weapon-comp (eu/comp-data system weapon EqWeapon)
        defenders (get-defenders system attacker)
        old-hit-list (:hit-list weapon-comp)
        entities-hit (coll/get-entity-collisions system (:hit-box weapon-comp) defenders)
        new-hit-list (into '() (clojure.set/difference (set entities-hit) (set old-hit-list)))
        hit-mobs (filter #(not= nil (eu/comp-data system % MobAI)) new-hit-list)]
    (if (not (empty? new-hit-list))
      (-> system
          (process-attack attacker weapon new-hit-list)
          (aggrivate hit-mobs))
      system)))

(defn update-entity-attacks
  "Updates the positions of the all weapon hit boxes belonging to attacking entities."
  [system attacker weapon]
  (let [weapon-comp (eu/comp-data system weapon EqWeapon)
        entity-space (eu/comp-data system attacker Spatial)
        new-hit-box ((weaponry/get-attack-fn (:type weapon-comp)) (:hit-box weapon-comp) entity-space)]
    (-> system
        (eu/comp-update weapon EqWeapon {:hit-box new-hit-box}))))

(defn update-combat-events
  "Updates information about attacks, etc."
  [system delta]
  (let [events (:combat (:world_events system))
        updated-events (map #(assoc % :delta (+ delta (:delta %))) events)
        filtered-events (filter #(> 1 (:delta %)) updated-events)]
    (assoc-in system [:world_events :combat] filtered-events)))

(defn update
  "Apply combat events and collisions"
  [system delta]
  ; TODO This doesn't feel right
  (let [updated-system (update-combat-events system delta)]
    (reduce
      (fn [sys attacker]
        (let [weapon-entity (:held (:equipment (eu/comp-data system attacker Equipable)))]
          (-> sys
              (update-entity-attacks attacker weapon-entity)
              (check-attack-collisions attacker weapon-entity)))) updated-system (eu/get-attackers updated-system))))
