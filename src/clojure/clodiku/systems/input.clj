(ns clodiku.systems.input
  (:import (clodiku.components Player State))
  (:require [clodiku.util.movement :as move]
            [clodiku.util.input :as input]
            [clodiku.entities.util :as eu]
            [clodiku.combat.core :as combat]))

(defn move-player [system delta]
  (let [player (eu/first-entity-with-comp system Player)
        move {:x (+ (if (input/pressed? :move-east) 2 0) (if (input/pressed? :move-west) -2 0))
              :y (+ (if (input/pressed? :move-north) 2 0) (if (input/pressed? :move-south) -2 0))}]
    (-> system
        (move/move-entity delta player move)
        (move/try-transport player move))))

(defn do-free-input
  [system delta]
  (if (input/pressed? :melee-attack)
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

(defn update [system delta events]
  (update-player system delta))

