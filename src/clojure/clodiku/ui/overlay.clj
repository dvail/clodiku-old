(ns clodiku.ui.overlay
  (:require [clodiku.entities.util :as eu])
  (:import (com.badlogic.gdx Gdx)
           (com.badlogic.gdx.scenes.scene2d.ui Label Skin Table Value$Fixed Cell)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.graphics Color)
           (clodiku.components Attribute)))

(declare overlay)

(defn init-ui!
  "Setup the user interface components"
  []
  (let [skin (Skin. (.internal Gdx/files "assets/ui/uiskin.json"))]
    (def overlay {:stage      (Stage.)
                  :main-table (Table.)
                  :hp-value   (Label. "0" skin "default-font" (Color. 0.0 1.0 0.0 1.0))
                  :mp-value   (Label. "0" skin "default-font" (Color. 0.0 0.0 1.0 1.0))})
    (let [attributes (Table.)]
      (.pad (.add attributes (:hp-value overlay)) (Value$Fixed. 5.0))
      (.pad (.add attributes (Label. "HP" skin)) (Value$Fixed. 5.0))
      (.pad (.add attributes (:mp-value overlay)) (Value$Fixed. 5.0))
      (.pad (.add attributes (Label. "MP" skin)) (Value$Fixed. 5.0))
      (doto attributes
        (.left)
        (.bottom)
        (.pack))
      (doto (:main-table overlay)
        (.setDebug true)
        (.setFillParent true)
        (.add attributes)
        (.left)
        (.bottom)
        (.pack)))
    (.addActor (:stage overlay) (:main-table overlay))))

(defn dispose!
  []
  (.dispose (:stage overlay)))

(defn update-ui!
  "Updates the user interface based on the state of the game entities"
  [system delta]
  (let [player-attr (eu/get-player-component system Attribute)]
    (.setText (:hp-value overlay) (str (:hp player-attr)))
    (.setText (:mp-value overlay) (str (:mp player-attr)))
    (doto (:stage overlay)
      (.act delta)
      (.draw))))