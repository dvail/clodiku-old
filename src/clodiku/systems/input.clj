(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input Input$Keys)
           (clodiku.components Player Spatial State))
  (:require [brute.entity :as be]
            [clodiku.components :as comps]
            [clodiku.util.collision :as coll]
            [clodiku.util.entities :as eu]))

(def bound-keys {:move_south   Input$Keys/S
                 :move_north   Input$Keys/W
                 :move_west    Input$Keys/A
                 :move_east    Input$Keys/D
                 :melee_attack Input$Keys/P})

(defn is-pressed? [k]
  (-> Gdx/input (.isKeyPressed (k bound-keys))))


(defn move-player [system delta]
  (let [player (first (be/get-all-entities-with-component system Player))
        pos (be/get-component system player Spatial)
        state (be/get-component system player State)
        mov-x (+ (if (is-pressed? :move_east) 2 0) (if (is-pressed? :move_west) -2 0))
        mov-y (+ (if (is-pressed? :move_north) 2 0) (if (is-pressed? :move_south) -2 0))
        newstate (if (= mov-x mov-y 0)
                   (comps/states :standing)
                   (comps/states :walking))
        newdelta (if (= newstate (:current state)) (+ delta (:time state)) 0)
        newdirection (cond
                       (= mov-x mov-y 0) (:direction pos)
                       (< 0 mov-x) (comps/directions :east)
                       (> 0 mov-x) (comps/directions :west)
                       (< 0 mov-y) (comps/directions :north)
                       (> 0 mov-y) (comps/directions :south))]
    ; TODO Change this to `be/update-component` -- seems like it must be more efficient
    (-> system
        (be/add-component player (comps/->Spatial
                                   (coll/get-movement-circle system (:pos pos) {:x mov-x :y mov-y}) newdirection))
        (be/add-component player (comps/->State newstate newdelta {})))))

(defn do-free-input [system delta]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (if (is-pressed? :melee_attack)
      (be/add-component system player (comps/->State (comps/states :melee) 0 {}))
      (move-player system delta))))

(defn do-melee-input
  [system delta]
  (let [player (first (be/get-all-entities-with-component system Player))
        old-state (be/get-component system player State)
        ; TODO Abstract out the time to stay in melee state - base this on Animation time
        new-state (if (< (:time old-state) 8/12)
                    (comps/->State (comps/states :melee) (+ (:time old-state) delta) {})
                    (comps/->State (comps/states :standing) 0 {}))]
    (-> system
        (be/add-component player new-state))))

(def process-input-for-state {:walking do-free-input
                              :standing do-free-input
                              :melee do-melee-input})

(defn update [system delta]
  (let [pstate (eu/get-player-component system State)]
    (-> system
        (((:current pstate) process-input-for-state) delta))))

