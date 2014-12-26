(ns clodiku.mainscreen
  (:require [clodiku.components :as comps]
            [clodiku.systems.input :as sys-input]
            [clodiku.systems.rendering :as sys-rendering]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Screen)
           (com.badlogic.gdx.math Circle)))

; Attach a simple map to the entity system to represent world state
(def system
  (atom (-> (be/create-system)
            (bs/add-system-fn sys-input/update)
            (bs/add-system-fn sys-rendering/render!))))

(defn init-map [sys]
  (reset! sys
          (let [tilemap (be/create-entity)]
            (-> @sys
                (be/add-entity tilemap)
                (be/add-component tilemap (comps/->WorldMap (clodiku.maps.map-core/load-map)))))))

(defn init-player [sys]
  (reset! sys
          (let [player (be/create-entity)
                regions (sys-rendering/split-texture-pack "./assets/player/player.pack")]
            (-> @sys
                (be/add-entity player)
                (be/add-component player (comps/->Player))
                (be/add-component player (comps/->Animated regions))
                (be/add-component player (comps/->State (comps/states :walking) 0.0 {}))
                (be/add-component player (comps/->Spatial
                                           (Circle. (float 100) (float 100) 14)
                                           (comps/directions :east)))))))

; TODO Entity and asset initialization...

(defn init-mobs [sys]
  (reset! sys
          (let [orc1 (be/create-entity)
                orc2 (be/create-entity)
                regions (sys-rendering/split-texture-pack "./assets/mob/orc/orc.pack")]
            (-> @sys
                (be/add-entity orc1)
                (be/add-component orc1 (comps/->MobAI))
                (be/add-component orc1 (comps/->Animated regions))
                (be/add-component orc1 (comps/->State (comps/states :walking) 0.0 {}))
                (be/add-component orc1 (comps/->Spatial
                                        (Circle. (float 200) (float 200) 14)
                                        (comps/directions :west)))
                (be/add-entity orc2)
                (be/add-component orc2 (comps/->MobAI))
                (be/add-component orc2 (comps/->Animated regions))
                (be/add-component orc2 (comps/->State (comps/states :walking) 0.0 {}))
                (be/add-component orc2 (comps/->Spatial
                                        (Circle. (float 500) (float 500) 14)
                                        (comps/directions :west)))))))

(defn screen []
  (proxy [Screen] []
    (show []
      (init-player system)
      (init-mobs system)
      (init-map system)
      (sys-rendering/init-resources! system))
    (render [delta]
      (reset! system (bs/process-one-game-tick @system delta)))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))
