(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle)
           (clodiku.components WorldMap))
  (:require [clodiku.components :as comps]
            [brute.entity :as be]
            [clodiku.entities.mobs :as em]
            [clodiku.util.rendering :as rendering]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.world.maps :as maps]
            [clodiku.entities.util :as eu]))


(def ^:const map-asset-dir "./assets/maps/")
(def ^:const map-data-file "/data.clj")
(def ^:const player-atlas "./assets/player/player.pack")

(defn init-map [sys map-name]
  (let [tmx-map (maps/load-map map-name)
        map-grid (maps/load-map-grid tmx-map)
        map-entity (eu/first-entity-with-comp sys WorldMap)]
    (if (nil? map-entity)
      (let [new-entity (be/create-entity)]
        (-> sys
            (be/add-entity new-entity)
            (be/add-component new-entity (comps/map->WorldMap {:tilemap tmx-map
                                                               :grid    map-grid}))))
      (-> sys
          (eu/comp-update map-entity WorldMap {:tilemap tmx-map
                                               :grid    map-grid})))))

(defn init-player [sys]
  (let [player (be/create-entity)
        weap (be/create-entity)
        armor (be/create-entity)
        regions (rendering/split-texture-pack player-atlas)]
    (-> sys
        (be/add-entity weap)
        (be/add-component weap (comps/map->Item {:name        "An emerald spear"
                                                 :description "This spear doesn't look very sharp"
                                                 :image       (rendering/make-texture "./assets/items/emerald-spear.png")}))
        (be/add-component weap (comps/map->EqItem {:hr   1
                                                   :slot (comps/eq-slots :held)}))
        (be/add-component weap (comps/map->EqWeapon
                                 {:base-damage 5
                                  :hit-box     (Circle. (float 0) (float 0) (float (:spear weaponry/weapon-sizes)))
                                  :hit-list    '()
                                  :type        (weaponry/weapon-types :spear)}))
        (be/add-entity armor)
        (be/add-component armor (comps/map->Item {:name        "Silver armor"
                                                  :description "This armor is made of silver"
                                                  :image       (rendering/make-texture "./assets/items/silver-scale-mail.png")}))
        (be/add-component armor (comps/map->EqItem {:ed   3
                                                    :slot (comps/eq-slots :body)}))
        (be/add-component armor (comps/map->EqArmor {:bulk 2}))
        (be/add-entity player)
        (be/add-component player (comps/map->Attribute {:hp  50
                                                        :mp  20
                                                        :mv  50
                                                        :str 10
                                                        :dex 10
                                                        :vit 10
                                                        :psy 10}))
        (be/add-component player (comps/map->Player {}))
        (be/add-component player (comps/map->AnimatedRenderable {:regions regions}))
        (be/add-component player (comps/map->State {:current (comps/states :walking)
                                                    :time    0.0}))
        (be/add-component player (comps/map->Equipment {:items {:held weap}}))
        (be/add-component player (comps/map->Inventory {:items (list armor)}))
        (be/add-component player (comps/map->Spatial {:pos       {:x 750 :y 660}
                                                      :size      14
                                                      :direction (comps/directions :east)})))))

(defn init-entities [system area-name]
  (binding [*read-eval* true]
    (let [area-data (->> (str map-asset-dir area-name map-data-file)
                         (slurp)
                         (read-string))
          items (:free-items area-data)
          mobs (:mobs area-data)]
      (reduce #(em/init-mob %1 %2) system mobs))))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-map "sample")
      (init-entities "sample")))

