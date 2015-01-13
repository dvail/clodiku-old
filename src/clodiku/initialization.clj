(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle))
  (:require [clodiku.components :as comps]
            [brute.entity :as be]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.equipment.weaponry :as weaponry]
            [clodiku.maps.map-core :as maps]))

(defn init-map [sys]
  (let [map-entity (be/create-entity)
        tmx-map (maps/load-map)
        map-grid (maps/load-map-grid tmx-map)]
    (-> sys
        (be/add-entity map-entity)
        (be/add-component map-entity (comps/->WorldMap tmx-map map-grid)))))

(defn init-player [sys]
  (let [player (be/create-entity)
        weap (be/create-entity)
        regions (sys-rendering/split-texture-pack "./assets/player/player.pack")]
    (-> sys
        (be/add-entity weap)
        (be/add-component weap (comps/->EqItem {:hr   1
                                                :slot (comps/eq-slots :held)}))
        (be/add-component weap (comps/->EqWeapon
                                 5                          ; Base damage
                                 (Circle. (float 0) (float 0) (float (:spear weaponry/weapon-sizes)))
                                 '()                        ; List of hit entities TODO Move this someplace else?
                                 (weaponry/weapon-types :spear)))
        (be/add-entity player)
        (be/add-component player (comps/->Player))
        (be/add-component player (comps/->Animated regions))
        (be/add-component player (comps/->State (comps/states :walking) 0.0 {}))
        (be/add-component player (comps/->Equipable {:held weap}))
        (be/add-component player (comps/->Spatial
                                   (Circle. (float 100) (float 100) 14)
                                   (comps/directions :east))))))

; TODO Entity and asset initialization...
(defn init-mobs [sys]
  (let [orc1 (be/create-entity)
        orc2 (be/create-entity)
        regions (sys-rendering/split-texture-pack "./assets/mob/orc/orc.pack")]
    (-> sys
        (be/add-entity orc1)
        (be/add-component orc1 (comps/->MobAI (comps/mob-ai-states :wander) {:last-update 0}))
        (be/add-component orc1 (comps/->Animated regions))
        (be/add-component orc1 (comps/->State (comps/states :walking) 0.0 {}))
        (be/add-component orc1 (comps/->Spatial
                                 (Circle. (float 300) (float 300) 14)
                                 (comps/directions :west)))
        (be/add-entity orc2)
        (be/add-component orc2 (comps/->MobAI (comps/mob-ai-states :wander) {:last-update 0}))
        (be/add-component orc2 (comps/->Animated regions))
        (be/add-component orc2 (comps/->State (comps/states :walking) 0.0 {}))
        (be/add-component orc2 (comps/->Spatial
                                 (Circle. (float 400) (float 400) 14)
                                 (comps/directions :west))))))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-mobs)
      (init-map)))

