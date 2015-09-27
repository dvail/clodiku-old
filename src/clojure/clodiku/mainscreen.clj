(ns clodiku.mainscreen
  (:require [clodiku.systems.input :as sys-input]
            [clodiku.systems.events :as sys-events]
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
            (bs/add-system-fn sys-events/process)
            (bs/add-system-fn sys-input/process)
            (bs/add-system-fn sys-mob-ai/process)
            (bs/add-system-fn sys-combat/process))))

(def events (atom {:ui '()
                   :combat '()
                   :animation '()}))

(defn screen []
  (proxy [Screen] []
    (show []
      (reset! system (init/init-main @system))
      (sys-rendering/init-resources! system)
      (reset! system (sys-rendering/init-skel-animation! @system))
      (ui/init-ui! system events))
    (render [delta]
        (reset! system (reduce #(%2 %1 delta events) @system (:system-fns @system)))
        (sys-rendering/render! @system delta events)
        (ui/update-ui! @system delta events))
    (dispose []
      (ui/dispose!))
    (hide [])
    (pause [])
    (resize [_ _])
    (resume [])))