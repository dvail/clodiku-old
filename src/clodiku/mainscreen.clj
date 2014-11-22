(ns clodiku.mainscreen
  (:require [clodiku.components :as comps]
            [clodiku.systems.input :as sys-input]
            [clodiku.systems.rendering :as sys-rendering]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Gdx Screen)))

; Attach a simple map to the entity system to represent world state
(def system
  (atom (assoc (-> (be/create-system)
                   (bs/add-system-fn sys-input/update)
                   (bs/add-system-fn sys-rendering/render!)) :world {})))

(defn init-player!
  []
  (reset! system (let [player (be/create-entity)]
    (-> @system
        (be/add-entity player)
        (be/add-component player (comps/->Player))
        (be/add-component player (comps/->Position 0 10))))))

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
