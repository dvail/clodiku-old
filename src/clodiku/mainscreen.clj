(ns clodiku.mainscreen
  (:require [clodiku.systems.input :as sys-input]
            [clodiku.systems.combat :as sys-combat]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.initialization :as init]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Screen)))

(def system
  (atom (-> (be/create-system)
            (bs/add-system-fn sys-input/update)
            (bs/add-system-fn sys-combat/update)
            (bs/add-system-fn sys-rendering/render!))))

(defn screen []
  (proxy [Screen] []
    (show []
      (reset! system (init/init-main @system))
      (reset! system (assoc @system :world_events '()))
      (sys-rendering/init-resources! system))
    (render [delta]
      (reset! system (bs/process-one-game-tick @system delta)))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))
