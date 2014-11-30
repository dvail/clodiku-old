(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input Input$Keys)
           (clodiku.components Player Spatial State)
           (com.badlogic.gdx.math Circle))
  (:require [brute.entity :as be]
            [clodiku.components :as comps]))

(def bound-keys {:move_south Input$Keys/S :move_north Input$Keys/W :move_west Input$Keys/A :move_east Input$Keys/D})

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
        (be/add-component player (comps/->Spatial (Circle.
                                                    (+ (.x ^Circle (:pos pos)) mov-x)
                                                    (+ (.y ^Circle (:pos pos)) mov-y) 18) newdirection))
        (be/add-component player (comps/->State newstate newdelta)))))

(defn update [system delta]
  (-> system (move-player delta)))