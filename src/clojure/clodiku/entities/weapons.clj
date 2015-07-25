(ns clodiku.entities.weapons
  (:require [clodiku.entities.components :as comps]))

(def weapon-templates {:sword {:item       {:name        "A short spear"
                                            :description "This short sword is dull"}
                               :renderable {:texture "./assets/items/steel-sword.png"}
                               :spatial    {:pos  {:x 0 :y 0}
                                            :size 10}
                               :eq-item     {:hr   1
                                            :slot :held}
                               :eq-weapon   {:base-damage 2
                                            :hit-box     {:x 0 :y 0 :size :sword}
                                            :hit-list    '()
                                            :type        :sword}}})

(defn init-weapon
  "Get a sequence of components based on a weapon keyword"
  [weapon]
  (if (keyword? weapon)
    (map #(apply comps/construct %) (weapon weapon-templates))
    (map #(apply comps/construct %) weapon)))

