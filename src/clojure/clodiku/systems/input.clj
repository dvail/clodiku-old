(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input Input$Keys)
           (clodiku.components Player State))
  (:require [clodiku.util.movement :as move]
            [clodiku.entities.util :as eu]
            [clodiku.combat.core :as combat]))

(def bound-keys {:move_south   Input$Keys/S
                 :move_north   Input$Keys/W
                 :move_west    Input$Keys/A
                 :move_east    Input$Keys/D
                 :melee_attack Input$Keys/P})

(defn is-pressed? [k]
  (-> Gdx/input (.isKeyPressed (k bound-keys))))

(defn move-player [system delta]
  (let [player (eu/first-entity-with-comp system Player)
        move {:x (+ (if (is-pressed? :move_east) 2 0) (if (is-pressed? :move_west) -2 0))
              :y (+ (if (is-pressed? :move_north) 2 0) (if (is-pressed? :move_south) -2 0))}]
    (-> system
        (move/move-entity delta player move)
        (move/try-transport player move))))

(defn do-free-input
  [system delta]
  (if (is-pressed? :melee_attack)
    (->> (eu/first-entity-with-comp system Player)
         (combat/init-attack system delta))
    (move-player system delta)))

(defmulti update-player (fn [system & _]  (:current (eu/get-player-component system State))))

(defmethod update-player :walking [system delta] (do-free-input system delta))

(defmethod update-player :standing [system delta]
  (do-free-input system delta))

(defmethod update-player :melee [system delta]
  (->> (eu/first-entity-with-comp system Player)
       (combat/advance-attack-state system delta)))

(defmethod update-player :default [system _] system)

(defn update [system delta]
  (update-player system delta))

