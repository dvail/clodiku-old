(ns clodiku.entities.weapons
  (:require [clodiku.combat.weaponry :as weaponry]
            [clodiku.components :as comps])
  (:import (com.badlogic.gdx.math Circle)))

(defn init-weapon
  "Get a sequence of components based on a mob keyword"
  [weapon-type]
  (eval (symbol (name weapon-type))))

(defn sword
  []
  [(comps/->EqItem {:hr   1
                    :slot (comps/eq-slots :held)})
   (comps/->EqWeapon {:base-damage 2
                      :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                      :hit-list    '()
                      :type        (weaponry/weapon-types :sword)})])
