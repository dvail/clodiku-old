(ns clodiku.mainscreen
  (:require [clodiku.systems.input :as sys-input]
            [clodiku.systems.mob-ai :as sys-mob-ai]
            [clodiku.systems.combat :as sys-combat]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.ui.core :as ui]
            [clodiku.initialization :as init]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Screen)))

(def system
  (atom (-> (be/create-system)
            (bs/add-system-fn sys-input/update)
            (bs/add-system-fn sys-mob-ai/update)
            (bs/add-system-fn sys-combat/update))))

(defn screen []
  (proxy [Screen] []
    (show []
      (reset! system (init/init-main @system))
      (reset! system (assoc @system :world_events {:combat '()}))
      (sys-rendering/init-resources! system)
      (ui/init-ui! system))
    (render [delta]
      (let [simulation (future (bs/process-one-game-tick @system delta))]
        (sys-rendering/render! @system delta)
        (ui/update-ui! @system delta)
        (reset! system @simulation)))
    (dispose []
      (ui/dispose!))
    (hide [])
    (pause [])
    (resize [_ _])
    (resume [])))
