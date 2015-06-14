(ns clodiku.ui.core
  (:require [clodiku.ui.hud :as hud]
            [clodiku.ui.menu :as menu])
  (:import (com.badlogic.gdx Gdx Input Files)
           (com.badlogic.gdx.scenes.scene2d.ui Skin Table Label VerticalGroup Container)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.graphics Color)))


(def ^:const ^String ui-json "assets/ui/uiskin.json")
(def ^:const ^String font-name "default-font")

(declare scene)

(defn stage
  "Creates a scene2d stage. Override all input processor methods in here."
  []
  (proxy [Stage] []))

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
    (hud/setup! system scene skin)
    (menu/setup! system scene skin)))

(defn dispose! [] (.dispose (:stage scene)))

(defn update-ui!
  "Updates the user interface based on the state of the game entities"
  [system delta]
  (hud/update! scene system delta)
  (menu/update! scene system delta)
  (doto (:stage scene)
    (.act delta)
    (.draw)))
