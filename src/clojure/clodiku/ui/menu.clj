(ns clodiku.ui.menu
  (:require [clodiku.entities.util :as eu])
  (:import (com.badlogic.gdx.scenes.scene2d.ui SplitPane VerticalGroup Label Container)))

(declare overlay)

(defn setup!
  "Setup the user interface components"
  [scene skin]
  (let [main-menu (VerticalGroup.)
        detail-menu (Container.)
        container (SplitPane. main-menu detail-menu false skin)]
    (doto main-menu
      (.addActor (Label. "Equipment" skin))
      (.addActor (Label. "Inventory" skin))
      (.addActor (Label. "Skills" skin))
      (.addActor (Label. "Stats" skin)))
    (doto (:menus scene)
      (.setDebug true)
      (.setFillParent true)
      (.add container)
      (.pack)))
  (.addActor (:stage scene) (:menus scene)))

(defn process-keypress
  [keycode]
  (println keycode)
  false)

(defn update!
  "Updates the user interface based on the state of the game entities"
  [scene system delta])