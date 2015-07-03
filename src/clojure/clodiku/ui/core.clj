(ns clodiku.ui.core
  (:require [clodiku.entities.util :as eu]
            [clodiku.ui.util :as uutil]
            [clojure.string :as s])
  (:import (com.badlogic.gdx Gdx Input Files)
           (com.badlogic.gdx.scenes.scene2d.ui Skin Table Label VerticalGroup Container Value$Fixed Image)
           (com.badlogic.gdx.scenes.scene2d Stage Touchable)
           (com.badlogic.gdx.graphics Color Texture)
           (clodiku.components Attribute Item Inventory Player Equipment)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)))


(def ^:const ^String ui-json "assets/ui/uiskin.json")
(def ^:const ^String font-name "default-font")

(declare scene)

(defn stage
  "Creates a scene2d stage. Override all input processor methods in here."
  []
  (proxy [Stage] []))

;
; Menu UI system functions
;
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
      (let [item-comp (eu/comp-data system item Item)
            item-text (Label. ^String (:name item-comp) skin)
            item-img (Image. ^Texture (:image item-comp))]
        (.row item-table)
        (.add item-table item-text)
        (.add item-table item-img)
        (.setTouchable item-text Touchable/enabled)
        (.addListener item-text (proxy [ClickListener] []
                                  (clicked [& _] (uutil/add-event system {:type :equip-item
                                                                                    :item item}))))))))

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
                    (clicked [& _] (open-submenu system scene skin
                                                 (keyword (s/lower-case name))))))))

(defn- populate-menu
  "Sets up the main menu options"
  [system scene menu skin]
  (doseq [name sub-menus]
    (.addActor menu (menu-button system scene name skin))))

(defn setup-menus!
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

(defn update-menus!
  "Updates the user interface based on the state of the game entities"
  [scene system delta])

;
; HUD UI functions
;

(defn init-hud!
  "Initializes the always on HUD"
  [system scene ^Skin skin]
  (let [attributes (Table.)]
    (.pad (.add attributes ^Label (:hp-value scene)) (Value$Fixed. 5.0))
    (.pad (.add attributes (Label. "HP" skin)) (Value$Fixed. 5.0))
    (.pad (.add attributes ^Label (:mp-value scene)) (Value$Fixed. 5.0))
    (.pad (.add attributes (Label. "MP" skin)) (Value$Fixed. 5.0))
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
  [scene system delta]
  (let [player-attr (eu/get-player-component system Attribute)]
    (.setText ^Label (:hp-value scene) (str (:hp player-attr)))
    (.setText ^Label (:mp-value scene) (str (:mp player-attr)))))

;
; Core UI functions
;

(defn init-ui!
  "Setup the user interface components"
  [system]
  (let [skin (Skin. (.internal ^Files Gdx/files ui-json))]
    (def scene {:overlay            (Table.)
                :menus              (Table.)
                :main-menu          (VerticalGroup.)
                :sub-menu-container (Container.)
                :sub-menus          {:equipment (Table.)
                                     :inventory (Table.)
                                     :skills    (VerticalGroup.)
                                     :stats     (VerticalGroup.)}
                :hp-value           (Label. "0" skin font-name (Color. 0.0 1.0 0.0 1.0))
                :mp-value           (Label. "0" skin font-name (Color. 0.0 0.0 1.0 1.0))
                :stage              (stage)})
    (.setInputProcessor ^Input Gdx/input (:stage scene))
    (init-hud! system scene skin)
    (setup-menus! system scene skin)))

(defn dispose! [] (.dispose (:stage scene)))

(defn update-ui!
  "Updates the user interface based on the state of the game entities"
  [system delta]
  (update-hud! scene system delta)
  (update-menus! scene system delta)
  (doto (:stage scene)
    (.act delta)
    (.draw)))
