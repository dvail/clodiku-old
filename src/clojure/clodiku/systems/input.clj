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

(defn do-free-input [system delta]
  (if (is-pressed? :melee_attack)
    (->> (eu/first-entity-with-comp system Player)
         (combat/init-attack system delta))
    (move-player system delta)))

; TODO Ideally this is where extra mid-attack skills could happen
(defn do-melee-input
  "Process input while in mid-attack."
  [system delta]
  (->> (eu/first-entity-with-comp system Player)
       (combat/advance-attack-state system delta)))

(defn update [system delta]
  (let [state (:current (eu/get-player-component system State))]
    (cond
      (= state :walking) (do-free-input system delta)
      (= state :standing) (do-free-input system delta)
      (= state :melee) (do-melee-input system delta)
      :else system)))