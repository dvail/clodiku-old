(ns clodiku.ui.core
  (:require [clodiku.entities.util :as eu]
            [clodiku.ui.util :as uutil]
            [clojure.string :as s]
            [clodiku.util.input :as input])
  (:import (com.badlogic.gdx Gdx Input Files)
           (com.badlogic.gdx.scenes.scene2d.ui Skin Table Label VerticalGroup Container Value$Fixed Image Value Cell)
           (com.badlogic.gdx.scenes.scene2d Stage Touchable)
           (com.badlogic.gdx.graphics Color Texture)
           (clodiku.entities.components Attribute Item Inventory Player Equipment Renderable)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)))


(def ^:const ^String ui-json "assets/ui/uiskin.json")
(def ^:const ^String font-name "default-font")
(def ^:const sub-menus ["Equipment" "Inventory" "Skills" "Stats"])

(declare ^Skin skin)
(declare scene)
(declare overlay)

(defn stage
  "Creates a scene2d stage. Override all input processor methods in here."
  []
  (proxy [Stage] []))

;;;
;;; Menu UI system functions.
;;;

(defmulti populate-action-menu "Show available actions for a submenu"
          (fn [_ _ name _] name))

(defmethod populate-action-menu :equipment
  [system events name entity])

(defmethod populate-action-menu :inventory
  [_ events _ entity]
  (let [action-table (:sub-menu-actions scene)
        equip-text (Label. "Equip" ^Skin skin)
        drop-text (Label. "Drop" ^Skin skin)]
    (.clear action-table)
    (.add action-table equip-text)
    (.pad ^Cell (.row action-table) 0.0 10.0 0.0 10.0)
    (.add action-table drop-text)
    (.setTouchable equip-text Touchable/enabled)
    (.setTouchable drop-text Touchable/enabled)
    (.addListener equip-text (proxy [ClickListener] []
                               (clicked [& _]
                                 (.clear (:sub-menu-actions scene))
                                 (uutil/add-event events {:type :equip-item
                                                          :item entity}))))
    (.addListener drop-text (proxy [ClickListener] []
                              (clicked [& _]
                                (.clear (:sub-menu-actions scene))
                                (uutil/add-event events {:type :drop-item
                                                         :item entity}))))))

(defmulti populate-sub-menu "A grouping of methods to populate the second level game menu"
          (fn [_ _ name] name))

(defmethod populate-sub-menu :equipment
  [system events _]
  (let [player (eu/first-entity-with-comp system Player)
        eq (:items (eu/comp-data system player Equipment))
        container (:sub-menu-container scene)
        eq-table (Table.)]
    (.setActor container eq-table)
    (doseq [slot (keys eq)]
      (.row eq-table)
      (.add eq-table (Label. (str slot) skin))
      (.pad (.add eq-table (Label. ^String (:name (eu/comp-data system (slot eq) Item)) ^Skin skin)) (Value$Fixed. 5.0))
      (.add eq-table (Image. ^Texture (:texture (eu/comp-data system (slot eq) Renderable)))))))

(defmethod populate-sub-menu :inventory
  [system events _]
  (let [player (eu/first-entity-with-comp system Player)
        items (:items (eu/comp-data system player Inventory))
        container (:sub-menu-container scene)
        item-table (Table.)]
    (.setActor container item-table)
    (doseq [item items]
      (let [item-comp (eu/comp-data system item Item)
            render-comp (eu/comp-data system item Renderable)
            item-text (Label. ^String (:name item-comp) ^Skin skin)
            item-img (Image. ^Texture (:texture render-comp))]
        (.row item-table)
        (.add item-table item-text)
        (.add item-table item-img)
        (.setTouchable item-text Touchable/enabled)
        (.addListener item-text (proxy [ClickListener] []
                                  (clicked [& _] (populate-action-menu system events :inventory item))))))))

(defmethod populate-sub-menu :default [_ _ name] (println (str "Uh oh - this isn't a real menu item: " name)))

(defn- open-submenu
  "Clears the existing menu pane and replaces with the pane referenced by the given name"
  [system events name]
  (let [container (:sub-menu-container scene)]
    (doto container
      (.clear)
      (.setActor (name (:sub-menus scene))))
    (populate-sub-menu @system events name)))

(defn- menu-button
  "Creates a menu button that listens for events to be passed to the UI"
  [system events ^String name]
  (doto (Label. name ^Skin skin)
    (.setTouchable Touchable/enabled)
    (.addListener (proxy [ClickListener] []
                    (clicked [& _] (open-submenu system events (keyword (s/lower-case name))))))))

(defn- populate-menu
  "Sets up the main menu options"
  [system events menu]
  (doseq [name sub-menus]
    (.addActor menu (menu-button system events name))))

(defn init-menus!
  "Setup the user interface components"
  [system events]
  (let [main-menu ^VerticalGroup (:main-menu scene)
        container ^Container (:sub-menu-container scene)
        actions ^Table (:sub-menu-actions scene)]
    (populate-menu system events main-menu)
    (.pad main-menu 10.0)
    (doto ^Table (:menus scene)
      (.setDebug true)
      (.setFillParent true)
      (.row)
      (.left)
      (.add main-menu)
      (.add container)
      (.add actions)
      (.pack)))
  (.addActor (:stage scene) (:menus scene)))

(defn update-menus!
  "Updates the user interface based on the state of the game entities"
  [system delta])

;;;
;;; HUD UI functions.
;;;

(defn init-hud!
  "Initializes the always on HUD"
  [system events]
  (let [attributes (Table.)]
    (.pad (.add attributes ^Label (:hp-value scene)) (Value$Fixed. 5.0))
    (.pad (.add attributes (Label. "HP" ^Skin skin)) (Value$Fixed. 5.0))
    (.pad (.add attributes ^Label (:mp-value scene)) (Value$Fixed. 5.0))
    (.pad (.add attributes (Label. "MP" ^Skin skin)) (Value$Fixed. 5.0))
    (doto attributes
      (.left)
      (.bottom)
      (.pack))
    (doto (:overlay scene)
      (.setDebug true)
      (.setFillParent true)
      (.add attributes)
      (.left)
      (.bottom)
      (.pack)))
  (.addActor (:stage scene) (:overlay scene)))

(defn update-hud!
  [system delta]
  (let [player-attr (eu/get-player-component system Attribute)]
    (.setText ^Label (:hp-value scene) (str (:hp player-attr)))
    (.setText ^Label (:mp-value scene) (str (:mp player-attr)))))

;;;
;;; Core UI functions.
;;;

(defn init-ui!
  "Setup the user interface components"
  [system events]
  (def skin (Skin. (.internal ^Files Gdx/files ui-json)))
  (def scene {:overlay            (Table.)
              :menus              (Table.)
              :main-menu          (VerticalGroup.)
              :sub-menu-container (Container.)
              :sub-menu-actions   (Table.)
              :hp-value           (Label. "0" ^Skin skin font-name (Color. 0.0 1.0 0.0 1.0))
              :mp-value           (Label. "0" ^Skin skin font-name (Color. 0.0 0.0 1.0 1.0))
              :stage              (stage)})
  (.setInputProcessor ^Input Gdx/input (:stage scene))
  (init-hud! system events)
  (init-menus! system events))

(defn dispose! [] (.dispose (:stage scene)))

(defn toggle-menus
  "Hide or show the main menu on the screen"
  []
  (let [menu (:menus scene)]
    (doto (:sub-menu-container scene)
      (.clear)
      (.setActor nil))
    (.clear (:sub-menu-actions scene))
    (.setVisible menu (not (.isVisible menu)))))

(defn update-ui!
  "Updates the user interface based on the state of the game entities"
  [system delta events]
  (update-hud! system delta)
  (update-menus! system delta)
  (when (input/just-pressed? :toggle-menus) (toggle-menus))
  (doto (:stage scene)
    (.act delta)
    (.draw)))