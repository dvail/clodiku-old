(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle))
  (:require [clodiku.components :as comps]
            [brute.entity :as be]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.maps.map-core :as maps]))

(defn init-map [sys]
  (let [map-entity (be/create-entity)
        tmx-map (maps/load-map)
        map-grid (maps/load-map-grid tmx-map)]
    (-> sys
        (be/add-entity map-entity)
        (be/add-component map-entity (comps/->WorldMap {:tilemap tmx-map
                                                        :grid    map-grid})))))

(defn init-player [sys]
  (let [player (be/create-entity)
        weap (be/create-entity)
        regions (sys-rendering/split-texture-pack "./assets/player/player.pack")]
    (-> sys
        (be/add-entity weap)
        (be/add-component weap (comps/->EqItem {:hr   1
                                                :slot (comps/eq-slots :held)}))
        (be/add-component weap (comps/->EqWeapon
                                 {:base-damage 5
                                  :hit-box     (Circle. (float 0) (float 0) (float (:spear weaponry/weapon-sizes)))
                                  :hit-list    '()
                                  :type        (weaponry/weapon-types :spear)}))
        (be/add-entity player)
        (be/add-component player (comps/->Attribute {:hp  50
                                                     :mv  50
                                                     :str 10
                                                     :dex 10
                                                     :vit 10
                                                     :psy 10}))
        (be/add-component player (comps/->Player {}))
        (be/add-component player (comps/->Animated {:regions regions}))
        (be/add-component player (comps/->State {:current (comps/states :walking)
                                                 :time    0.0}))
        (be/add-component player (comps/->Equipable {:equipment {:held weap}}))
        (be/add-component player (comps/->Spatial {:pos       {:x 100 :y 100}
                                                   :size      14
                                                   :direction (comps/directions :east)})))))

; TODO Entity and asset initialization...
(defn init-mobs [sys]
  (let [orc1 (be/create-entity)
        orc2 (be/create-entity)
        weap-a (be/create-entity)
        weap-b (be/create-entity)
        regions (sys-rendering/split-texture-pack "./assets/mob/orc/orc.pack")]
    (-> sys
        (be/add-entity weap-a)
        (be/add-component weap-a (comps/->EqItem {:hr   1
                                                  :slot (comps/eq-slots :held)}))
        (be/add-component weap-a (comps/->EqWeapon {:base-damage 2
                                                    :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                                                    :hit-list    '()
                                                    :type        (weaponry/weapon-types :sword)}))
        (be/add-entity weap-b)
        (be/add-component weap-b (comps/->EqItem {:hr   1
                                                  :slot (comps/eq-slots :held)}))
        (be/add-component weap-b (comps/->EqWeapon {:base-damage 2
                                                    :hit-box     (Circle. (float 0) (float 0) (float (:sword weaponry/weapon-sizes)))
                                                    :hit-list    '()
                                                    :type        (weaponry/weapon-types :sword)}))

        (be/add-entity orc1)
        (be/add-component orc1 (comps/->Attribute {:hp  30
                                                   :mv  50
                                                   :str 14
                                                   :dex 8
                                                   :vit 14
                                                   :psy 3}))
        (be/add-component orc1 (comps/->MobAI {:state       (comps/mob-ai-states :wander)
                                               :last-update 0
                                               :path        '()}))
        (be/add-component orc1 (comps/->Animated {:regions regions}))
        (be/add-component orc1 (comps/->Equipable {:equipment {:held weap-a}}))

        (be/add-component orc1 (comps/->State {:current (comps/states :walking)
                                               :time    0.0}))
        (be/add-component orc1 (comps/->Spatial {:pos       {:x 300 :y 300}
                                                 :size      14
                                                 :direction (comps/directions :west)}))

        (be/add-entity orc2)
        (be/add-component orc2 (comps/->Attribute {:hp  30
                                                   :mv  50
                                                   :str 14
                                                   :dex 8
                                                   :vit 14
                                                   :psy 3}))
        (be/add-component orc2 (comps/->MobAI {:state       (comps/mob-ai-states :wander)
                                               :last-update 0
                                               :path        '()}))
        (be/add-component orc2 (comps/->Animated {:regions regions}))
        (be/add-component orc2 (comps/->Equipable {:equipment {:held weap-b}}))
        (be/add-component orc2 (comps/->State {:current (comps/states :walking)
                                               :time    0.0}))
        (be/add-component orc2 (comps/->Spatial {:pos       {:x 400 :y 400}
                                                 :size      14
                                                 :direction (comps/directions :west)})))))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-mobs)
      (init-map)))

