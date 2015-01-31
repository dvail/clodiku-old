(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input Input$Keys)
           (clodiku.components Player Spatial State EqWeapon Equipable))
  (:require [clodiku.components :as comps]
            [clodiku.util.movement :as move]
            [clodiku.util.entities :as eu]
            [clodiku.equipment.weaponry :as weaponry]))

(def bound-keys {:move_south   Input$Keys/S
                 :move_north   Input$Keys/W
                 :move_west    Input$Keys/A
                 :move_east    Input$Keys/D
                 :melee_attack Input$Keys/P})

(defn is-pressed? [k]
  (-> Gdx/input (.isKeyPressed (k bound-keys))))

(defn begin-attack [system delta]
  (let [player (eu/first-entity-with-comp system Player)
        spatial (eu/comp-data system player Spatial)
        eq-weapon (:held (:equipment (eu/comp-data system player Equipable)))
        weapon-data (eu/comp-data system eq-weapon EqWeapon)]
    (-> system
        (eu/comp-update player State {:current (comps/states :melee)
                                      :time    0})
        (eu/comp-update eq-weapon EqWeapon {:hit-box  (weaponry/get-attack-start-pos (:type weapon-data) spatial)
                                            :hit-list '()}))))

(defn move-player [system delta]
  (let [player (eu/first-entity-with-comp system Player)
        move {:x (+ (if (is-pressed? :move_east) 2 0) (if (is-pressed? :move_west) -2 0))
              :y (+ (if (is-pressed? :move_north) 2 0) (if (is-pressed? :move_south) -2 0))}]
    (move/move-entity system delta player move)))

(defn do-free-input [system delta]
  (if (is-pressed? :melee_attack)
    (begin-attack system delta)
    (move-player system delta)))

(defn do-melee-input
  [system delta]
  (let [player (eu/first-entity-with-comp system Player)
        old-state (eu/comp-data system player State)
        ; TODO Abstract out the time to stay in melee state - base this on Animation time
        new-state (if (< (:time old-state) 4/12)
                    {:current (comps/states :melee)
                     :time    (+ (:time old-state) delta)}
                    {:current (comps/states :standing)
                     :time    0})]
    (-> system
        (eu/comp-update player State new-state))))

(def process-input-for-state {:walking  do-free-input
                              :standing do-free-input
                              :melee    do-melee-input})

(defn update [system delta]
  (let [pstate (eu/get-player-component system State)]
    (-> system
        (((:current pstate) process-input-for-state) delta))))
