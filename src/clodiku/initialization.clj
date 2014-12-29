(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle Rectangle))
  (:require [clodiku.components :as comps]
            [brute.entity :as be]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.equipment.weaponry :as weaponry]))

(defn init-map [sys]
  (let [tilemap (be/create-entity)]
    (-> sys
        (be/add-entity tilemap)
        (be/add-component tilemap (comps/->WorldMap (clodiku.maps.map-core/load-map))))))

(defn init-player [sys]
  (let [player (be/create-entity)
        spear (be/create-entity)
        regions (sys-rendering/split-texture-pack "./assets/player/player.pack")]
    (-> sys
        (be/add-entity spear)
        (be/add-component spear (comps/->EqItem {:damage 5
                                                 :hr 1
                                                 :slot (comps/eq-slots :held)}))
        (be/add-component spear (comps/->EqWeapon
                                  (Circle. (float 0) (float 0) (float 4))
                                  (:spear weaponry/attack-fns)))
        (be/add-entity player)
        (be/add-component player (comps/->Player))
        (be/add-component player (comps/->Animated regions))
        (be/add-component player (comps/->State (comps/states :walking) 0.0 {}))
        (be/add-component player (comps/->Equipable {:held spear}))
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
        (be/add-component orc1 (comps/->MobAI))
        (be/add-component orc1 (comps/->Animated regions))
        (be/add-component orc1 (comps/->State (comps/states :walking) 0.0 {}))
        (be/add-component orc1 (comps/->Spatial
                                 (Circle. (float 300) (float 300) 14)
                                 (comps/directions :west)))
        (be/add-entity orc2)
        (be/add-component orc2 (comps/->MobAI))
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

