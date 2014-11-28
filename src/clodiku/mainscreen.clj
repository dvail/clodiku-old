(ns clodiku.mainscreen
  (:require [clodiku.components :as comps]
            [clodiku.systems.input :as sys-input]
            [clodiku.systems.rendering :as sys-rendering]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Gdx Screen)
           (com.badlogic.gdx.graphics.g2d TextureAtlas)
           (com.badlogic.gdx.math Circle)))

; Attach a simple map to the entity system to represent world state
(def system
  (atom (assoc (-> (be/create-system)
                   (bs/add-system-fn sys-input/update)
                   (bs/add-system-fn sys-rendering/render!)) :world {})))

(defn init-player!
  []
  (reset! system (let [player (be/create-entity)
                       regions (sys-rendering/split-texture-pack "./assets/player/player.pack")]
                   (-> @system
                       (be/add-entity player)
                       (be/add-component player (comps/->Player))
                       (be/add-component player (comps/->AnimationMap regions))
                       (be/add-component player (comps/->State (comps/states :walking) 0.0))
                       (be/add-component player (comps/->Spatial (Circle. (float 100) (float 100) 18) (comps/directions :east)))))))

(defn screen []
  (proxy [Screen] []
    (show []
      (sys-rendering/init-resources!)
      (init-player!))
    (render [delta]
      (reset! system (bs/process-one-game-tick @system delta)))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))
