(ns clodiku.ui.menu
  (:require [clodiku.entities.util :as eu])
  (:import (com.badlogic.gdx.scenes.scene2d.ui SplitPane VerticalGroup Label Container Skin)
           (com.badlogic.gdx.scenes.scene2d Touchable)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)))

(declare overlay)

(def ^:const sub-menus ["Equipment" "Inventory" "Skills" "Stats"])

(defn open-submenu
  "Clears the existing menu pane and replaces with the pane referenced by the given name"
  [name]
  (println name))

(defn menu-button
  "Creates a menu button that listens for events to be passed to the UI"
  [^String name ^Skin skin]
  (doto (Label. name skin)
    (.setTouchable Touchable/enabled)
    (.addListener (proxy [ClickListener] [] (clicked [& _] (open-submenu (keyword name)))))))

(defn- populate-menu
  "Sets up the main menu options"
  [menu skin]
  (doseq [name sub-menus]
    (.addActor menu (menu-button name skin))))

(defn setup!
  "Setup the user interface components"
  [scene skin]
  (let [main-menu (VerticalGroup.)
        detail-menu (Container.)
        container (SplitPane. main-menu detail-menu false ^Skin skin)]
    (populate-menu main-menu skin)
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