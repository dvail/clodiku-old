(ns clodiku.ui.menu
  (:require [clodiku.entities.util :as eu]
            [clojure.string :as s])
  (:import (com.badlogic.gdx.scenes.scene2d.ui SplitPane VerticalGroup Label Container Skin Value$Fixed Table Image)
           (com.badlogic.gdx.scenes.scene2d Touchable)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)
           (clodiku.components Player Equipment Item Inventory)
           (com.badlogic.gdx.graphics Texture)))

(declare overlay)

(def ^:const sub-menus ["Equipment" "Inventory" "Skills" "Stats"])

(defmulti populate-sub-menu "A grouping of methods to pupulate the second level game menu"
          (fn [_ _ _ name] name))

(defmethod populate-sub-menu :equipment
  [system scene ^Skin skin _]
  (let [player (eu/first-entity-with-comp system Player)
        eq (:items (eu/comp-data system player Equipment))
        container (:sub-menu-container scene)
        eq-table (Table.)]
    (.setActor container eq-table)
    (doseq [slot (keys eq)]
      (.row eq-table)
      (.add eq-table (Label. (str slot) skin))
      (.pad (.add eq-table (Label. ^String (:name (eu/comp-data system (slot eq) Item)) skin)) (Value$Fixed. 5.0))
      (.add eq-table (Image. ^Texture (:image (eu/comp-data system (slot eq) Item)))))))

(defmethod populate-sub-menu :inventory
  [system scene ^Skin skin _]
  (let [player (eu/first-entity-with-comp system Player)
        items (:items (eu/comp-data system player Inventory))
        container (:sub-menu-container scene)
        item-table (Table.)]
    (.setActor container item-table)
    (doseq [item items]
      (let [item-comp (eu/comp-data system item Item)]
        (.row item-table)
        (.add item-table (Label. ^String (:name item-comp) skin))
        (.add item-table (Image. ^Texture (:image item-comp)))))))

(defmethod populate-sub-menu :default [_ _ _ name] (println (str "Uh oh - this isn't a real menu item: " name)))

(defn- open-submenu
  "Clears the existing menu pane and replaces with the pane referenced by the given name"
  [system scene skin name]
  (let [container (:sub-menu-container scene)]
    (doto container
      (.clear)
      (.setActor (name (:sub-menus scene))))
    (populate-sub-menu @system scene skin name)))

(defn- menu-button
  "Creates a menu button that listens for events to be passed to the UI"
  [system scene ^String name ^Skin skin]
  (doto (Label. name skin)
    (.setTouchable Touchable/enabled)
    (.addListener (proxy [ClickListener] []
                    (clicked [& _] (open-submenu system scene skin (keyword (s/lower-case name))))))))

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
      (.left)
      (.add main-menu)
      (.add container)
      (.pack)))
  (.addActor (:stage scene) (:menus scene)))

(defn update!
  "Updates the user interface based on the state of the game entities"
  [scene system delta])