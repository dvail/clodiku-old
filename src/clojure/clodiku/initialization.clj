(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle))
  (:require [clodiku.components :as comps]
            [brute.entity :as be]
            [clodiku.entities.mobs :as em]
            [clodiku.entities.weapons :as ew]
            [clodiku.util.rendering :as rendering]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.world.maps :as maps]
            [clojure.tools.reader.edn :as edn]))

(defn init-map [sys map-name]
  (let [map-entity (be/create-entity)
        tmx-map (maps/load-map map-name)
        map-grid (maps/load-map-grid tmx-map)]
    (-> sys
        (be/add-entity map-entity)
        (be/add-component map-entity (comps/map->WorldMap {:tilemap tmx-map
                                                        :grid    map-grid})))))

(defn init-player [sys]
  (let [player (be/create-entity)
        weap (be/create-entity)
        regions (rendering/split-texture-pack "./assets/player/player.pack")]
    (-> sys
        (be/add-entity weap)
        (be/add-component weap (comps/map->EqItem {:hr   1
                                                :slot (comps/eq-slots :held)}))
        (be/add-component weap (comps/map->EqWeapon
                                 {:base-damage 5
                                  :hit-box     (Circle. (float 0) (float 0) (float (:spear weaponry/weapon-sizes)))
                                  :hit-list    '()
                                  :type        (weaponry/weapon-types :spear)}))
        (be/add-entity player)
        (be/add-component player (comps/map->Attribute {:hp  50
                                                     :mv  50
                                                     :str 10
                                                     :dex 10
                                                     :vit 10
                                                     :psy 10}))
        (be/add-component player (comps/map->Player {}))
        (be/add-component player (comps/map->Animated {:regions regions}))
        (be/add-component player (comps/map->State {:current (comps/states :walking)
                                                 :time    0.0}))
        (be/add-component player (comps/map->Equipable {:equipment {:held weap}}))
        (be/add-component player (comps/map->Spatial {:pos       {:x 100 :y 100}
                                                   :size      14
                                                   :direction (comps/directions :east)})))))

; TODO Entity and asset initialization...
(defn init-entities [system area-name]
  (let [area-data (->> (str "assets/maps/" area-name "/data.edn")
                       (slurp)
                       (edn/read-string))
        items (:free-items area-data)
        mobs (:mobs area-data)]
    (reduce (fn [sys mob]
              (em/init-mob sys mob)) system mobs)))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-map "sample")
      (init-entities "sample")))

