(ns clodiku.systems.combat
  (:import (clodiku.entities.components Spatial EqWeapon Equipment Player MobAI Attribute State))
  (:require [clodiku.entities.util :as eu]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.util.movement :as coll]
            [clodiku.combat.calculations :as ccalc]
            [clodiku.entities.components :as comps]))

(defn aggrivate
  "Changes the behavior of all mobs sent as input to aggressive."
  [system mobs]
  (reduce (fn [sys mob] (eu/comp-update sys mob MobAI {:state (comps/mob-ai-states :aggro)})) system mobs))

(defn get-defenders
  "Gets all possible entities that could be hit by an attack, excluding the
  initiator of the attack"
  [system attacker]
  (let [defenders (eu/get-entities-with-components system Player MobAI)
        filter-type (if (eu/has-comp? system attacker Player)
                      Player
                      MobAI)]
    (clojure.set/difference (set defenders) (set (eu/get-entities-with-components system filter-type)))))

(defn damage-entity
  "Apply damage to an entity, changing state to DEAD if hp falls below zero."
  [system entity damage]
  (let [old-hp (:hp (eu/comp-data system entity Attribute))
        new-hp (max 0 (- old-hp damage))
        state (if (<= new-hp 0)
                (comps/states :dead)
                (:current (eu/comp-data system entity State)))]
    (-> system
        (eu/comp-update entity Attribute {:hp new-hp})
        (eu/comp-update entity State {:current state}))))

(defn process-attack
  "Dispatch events attack and apply damage to affected entities."
  [system events attacker weapon hit-list]
  ; TODO Apply damage, etc. here
  ; TODO separate the event handling part out of here if possible
  (reduce (fn [sys hit-entity]
            (let [damage (ccalc/attack-damage sys attacker hit-entity)
                  event {:type     :melee
                         :attacker attacker
                         :defender hit-entity
                         :location (:pos (eu/comp-data sys hit-entity Spatial))
                         :damage   damage
                         :delta    0}
                  combat-events (:combat @events)
                  new-event-list (conj combat-events event)
                  old-hit-list (:hit-list (eu/comp-data system weapon EqWeapon))]
              (swap! events #(assoc %1 :combat %2) new-event-list)
              (-> sys
                  (damage-entity hit-entity damage)
                  (eu/comp-update weapon EqWeapon {:hit-list (conj old-hit-list hit-entity)})))) system hit-list))

(defn check-attack-collisions
  "Tests is any entities are hit by a weapon. Does not allow an entity
  to hit him/herself when attacking"
  [system events attacker weapon]
  (let [weapon-comp (eu/comp-data system weapon EqWeapon)
        defenders (get-defenders system attacker)
        old-hit-list (:hit-list weapon-comp)
        entities-hit (coll/get-entity-collisions system (:hit-box weapon-comp) defenders)
        new-hit-list (into '() (clojure.set/difference (set entities-hit) (set old-hit-list)))
        hit-mobs (filter #(not= nil (eu/comp-data system % MobAI)) new-hit-list)]
    (if (not (empty? new-hit-list))
      (-> system
          (process-attack events attacker weapon new-hit-list)
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

(defn apply-regen
  ; TODO Implement
  "Applies interval based regneration of hp, mp, etc. to entities"
  [system]
  system)

(defn process
  "Apply combat events and collisions"
  [system delta events]
  ; TODO This doesn't feel right
  (reduce
    (fn [sys attacker]
      (let [weapon-entity (-> (eu/comp-data system attacker Equipment)
                              :items
                              :held)]
        (-> sys
            (apply-regen)
            (update-entity-attacks attacker weapon-entity)
            (check-attack-collisions events attacker weapon-entity)))) system (eu/get-attackers system)))
