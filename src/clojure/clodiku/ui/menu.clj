(ns clodiku.ui.menu
  (:require [clodiku.entities.util :as eu]
            [clojure.string :as s])
  (:import (com.badlogic.gdx.scenes.scene2d.ui SplitPane VerticalGroup Label Container Skin Value$Fixed Table)
           (com.badlogic.gdx.scenes.scene2d Touchable)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)
           (clodiku.components Player Equipable)))

(declare overlay)

(def ^:const sub-menus ["Equipment" "Inventory" "Skills" "Stats"])

(defmulti populate-sub-menu (fn [_ _ name] name))

(defmethod populate-sub-menu :equipment
  [system scene name]
  (let [player (eu/first-entity-with-comp system Player)
        eq (:equipment (eu/comp-data system player Equipable))]
    (doseq [slot (keys eq)]
      (println slot))))

(defmethod populate-sub-menu :default [_ _ name] (println (str "Uh oh - this isn't a real menu item: " name)))

(defn- open-submenu
  "Clears the existing menu pane and replaces with the pane referenced by the given name"
  [system scene name]
  (let [container (:sub-menu-container scene)]
    (doto container
      (.clear)
      (.setActor (name (:sub-menus scene))))
    (populate-sub-menu @system scene name)))

(defn- menu-button
  "Creates a menu button that listens for events to be passed to the UI"
  [system scene ^String name ^Skin skin]
  (doto (Label. name skin)
    (.setTouchable Touchable/enabled)
    (.addListener (proxy [ClickListener] []
                    (clicked [& _] (open-submenu system scene (keyword (s/lower-case name))))))))

(defn- populate-menu
  "Sets up the main menu options"
  [system scene menu skin]
  (doseq [name sub-menus]
    (.addActor menu (menu-button system scene name skin))))

(defn setup!
  "Setup the user interface components"
  [system scene skin]
  (let [main-menu ^VerticalGroup (:main-menu scene)
        container ^Container (:sub-menu-container scene)]
    (populate-menu system scene main-menu skin)
    (.pad main-menu 10.0)
    (doto ^Table (:menus scene)
      (.setDebug true)
      (.setFillParent true)
      (.row)
      (.add main-menu)
      (.row)
      (.add container)
      (.pack)))
  (.addActor (:stage scene) (:menus scene)))

(defn update!
  "Updates the user interface based on the state of the game entities"
  [scene system delta])