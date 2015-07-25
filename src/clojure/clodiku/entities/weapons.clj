(ns clodiku.entities.weapons
  (:require [clodiku.entities.components :as comps]
            [brute.entity :as be]))

(def templates {:sword {:item       {:name        "A short spear"
                                     :description "This short sword is dull"}
                        :renderable {:texture "./assets/items/steel-sword.png"}
                        :spatial    {:pos  {:x 0 :y 0}
                                     :size 10}
                        :eq-item    {:hr   1
                                     :slot :held}
                        :eq-weapon  {:base-damage 2
                                     :hit-box     {:x 0 :y 0 :size :sword}
                                     :hit-list    '()
                                     :type        :sword}}})

; TODO This is duplicated from the 'mobs' namespace
(defn merge-default-components
  "Evaluates the default template components and overridden components and merges them into a component set."
  [item]
  (let [item-type (:template item)
        item-comp-map (item-type templates)]
    (merge (comps/construct-map item-comp-map)
           (comps/construct-map (:components item)))))

(defn init-item-comps
  "Get a sequence of components based on an item keyword"
  [item]
  (if (keyword? item)
    (map #(apply comps/construct %) (item templates))
    (vals (merge-default-components item))))

(defn bind-item
  [system item-comps]
  (let [item (be/create-entity)]
    (println item-comps)
    (reduce #(be/add-component %1 item %2) system item-comps)))