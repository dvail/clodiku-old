(ns clodiku.entities.weapons
  (:require [clodiku.combat.weaponry :as weaponry]
            [clodiku.components :as comps])
  (:import (com.badlogic.gdx.math Circle)))

(def weapon-templates {:sword (fn template-components []
                                {:eqitem   (comps/map->EqItem {:hr   1
                                                               :slot (comps/eq-slots :held)})
                                 :eqweapon (comps/map->EqWeapon {:base-damage 2
                                                                 :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                                                                 :hit-list    '()
                                                                 :type        (weaponry/weapon-types :sword)})})})

(defn init-weapon
  "Get a sequence of components based on a weapon keyword"
  [weapon]
  (if (keyword? weapon)
    ((eval (weapon weapon-templates)))
    ((eval weapon))))

