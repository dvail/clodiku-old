(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input Input$Keys)
           (clodiku.components Player Spatial State EqWeapon Equipable))
  (:require [clodiku.components :as comps]
            [clodiku.util.collision :as coll]
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
        spatial (eu/comp-data system player Spatial)
        state (eu/comp-data system player State)
        mov-x (+ (if (is-pressed? :move_east) 2 0) (if (is-pressed? :move_west) -2 0))
        mov-y (+ (if (is-pressed? :move_north) 2 0) (if (is-pressed? :move_south) -2 0))
        newstate (if (= mov-x mov-y 0)
                   (comps/states :standing)
                   (comps/states :walking))
        newdelta (if (= newstate (:current state)) (+ delta (:time state)) 0)
        newdirection (cond
                       (= mov-x mov-y 0) (:direction spatial)
                       (< 0 mov-x) (comps/directions :east)
                       (> 0 mov-x) (comps/directions :west)
                       (< 0 mov-y) (comps/directions :north)
                       (> 0 mov-y) (comps/directions :south))]
    ; be/add-component is more efficient here - update component is only more ideal when we need to retain old
    ; parameter values
    (-> system
        (eu/comp-update player Spatial {:pos       (coll/get-movement-map system spatial {:x mov-x :y mov-y})
                                        :direction newdirection})
        (eu/comp-update player State {:current newstate
                                      :time    newdelta}))))

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
        (eu/comp-update player State new-state)
        )))

(def process-input-for-state {:walking  do-free-input
                              :standing do-free-input
                              :melee    do-melee-input})

(defn update [system delta]
  (let [pstate (eu/get-player-component system State)]
    (-> system
        (((:current pstate) process-input-for-state) delta))))
