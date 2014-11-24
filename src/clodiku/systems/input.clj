(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input$Keys Input)
           (clodiku.components Player Position))
  (:require [brute.entity :as be]
            [clodiku.components :as comps]))

(def bound-keys {:move_south Input$Keys/S :move_north Input$Keys/W :move_west Input$Keys/A :move_east Input$Keys/D})

(defn is-pressed? [k]
  (-> Gdx/input (.isKeyPressed (k bound-keys))))

(defn move-player [system]
  (let [player (first (be/get-all-entities-with-component system Player))
        pos (be/get-component system player Position)
        mov-x (+ (if (is-pressed? :move_east) 2 0) (if (is-pressed? :move_west) -2 0))
        mov-y (+ (if (is-pressed? :move_north) 2 0) (if (is-pressed? :move_south) -2 0))]
    (be/add-component system player (comps/->Position (+ (:x pos) mov-x) (+ (:y pos) mov-y)))))

(defn update [system delta]
  (-> system (move-player)))