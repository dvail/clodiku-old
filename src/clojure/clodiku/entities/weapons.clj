(ns clodiku.entities.weapons
  (:require [clodiku.combat.weaponry :as weaponry]
            [clodiku.entities.components :as comps])
  (:import (com.badlogic.gdx.math Circle)))

(def weapon-templates {:sword {:item       {:name        "A short spear"
                                            :description "This short sword is dull"}
                               :renderable {:texture "./assets/items/steel-sword.png"}
                               :spatial    {:pos  {:x 0 :y 0}
                                            :size 10}
                               :eq-item     {:hr   1
                                            :slot :held}
                               :eq-weapon   {:base-damage 2
                                            :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                                            :hit-list    '()
                                            :type        :sword}}})

(defn init-weapon
  "Get a sequence of components based on a weapon keyword"
  [weapon]
  (if (keyword? weapon)
    (map #(apply comps/construct %) (weapon weapon-templates))
    (map #(apply comps/construct %) weapon)))

