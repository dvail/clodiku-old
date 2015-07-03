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

(def events (atom {:ui-events '()
                   :world-events {:combat '()}}))

(defn screen []
  (proxy [Screen] []
    (show []
      (reset! system (init/init-main @system))
      (sys-rendering/init-resources! system)
      (ui/init-ui! system events))
    (render [delta]
        (reset! system (reduce (fn [sys sys-fn] (sys-fn sys delta events))
                               @system (:system-fns @system)))
        (sys-rendering/render! @system delta events)
        (ui/update-ui! @system delta))
    (dispose []
      (ui/dispose!))
    (hide [])
    (pause [])
    (resize [_ _])
    (resume [])))
