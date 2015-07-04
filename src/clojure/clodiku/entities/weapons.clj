(ns clodiku.entities.weapons
  (:require [clodiku.combat.weaponry :as weaponry]
            [clodiku.entities.components :as comps]
            [clodiku.util.rendering :as rendering])
  (:import (com.badlogic.gdx.math Circle)))

(def weapon-templates {:sword (fn template-components []
                                {:item     (comps/map->Item {:name        "A short spear"
                                                             :description "This short sword is dull"
                                                             :image       (rendering/make-texture "./assets/items/steel-sword.png")})
                                 :eqitem   (comps/map->EqItem {:hr   1
                                                               :slot (comps/eq-slots :held)})
                                 :eqweapon (comps/map->EqWeapon {:base-damage 2
                                                                 :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                                                                 :hit-list    '()
                                                                 :type        (weaponry/weapon-types :sword)})})})

(defn init-weapon
  "Get a sequence of components based on a weapon keyword"
  [weapon]
  (if (keyword? weapon)
    ((weapon weapon-templates))
    ((weapon))))

