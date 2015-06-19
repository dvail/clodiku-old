(ns clodiku.combat.core
  (:require [clodiku.combat.weaponry :as weaponry]
            [clodiku.entities.util :as eu]
            [clodiku.components :as comps])
  (:import (clodiku.components EqWeapon State Equipment Spatial)))

(defn advance-attack-state
  "Updates an entity's attack state - so that it can be animated, end, etc."
  [system delta entity]
  (let [old-state (eu/comp-data system entity State)
        ; TODO Abstract out the time to stay in melee state - base this on Animation time
        new-state (if (< (:time old-state) 4/12)
                    {:current (comps/states :melee)
                     :time    (+ (:time old-state) delta)}
                    {:current (comps/states :standing)
                     :time    0})]
    (eu/comp-update system entity State new-state)))


(defn init-attack
  "Start the attack state for an entity"
  [system delta entity]
  (let [spatial (eu/comp-data system entity Spatial)
        eq-weapon (:held (:items (eu/comp-data system entity Equipment)))
        weapon-data (eu/comp-data system eq-weapon EqWeapon)]
    (-> system
        (eu/comp-update entity State {:current (comps/states :melee)
                                      :time    0})
        (eu/comp-update eq-weapon EqWeapon {:hit-box  (weaponry/get-attack-start-pos (:type weapon-data) spatial)
                                            :hit-list '()}))))