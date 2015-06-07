(ns clodiku.ui.hud
  (:require [clodiku.entities.util :as eu])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Value$Fixed Label Table)
           (clodiku.components Attribute)))

(defn setup!
  "initializes the HUD overlay on the screen"
  [scene skin]
  (let [attributes (Table.)]
    (.pad (.add attributes (:hp-value scene)) (Value$Fixed. 5.0))
    (.pad (.add attributes (Label. "HP" skin)) (Value$Fixed. 5.0))
    (.pad (.add attributes (:mp-value scene)) (Value$Fixed. 5.0))
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

(defn update!
  [scene system delta]
  (let [player-attr (eu/get-player-component system Attribute)]
    (.setText ^Label (:hp-value scene) (str (:hp player-attr)))
    (.setText ^Label (:mp-value scene) (str (:mp player-attr)))))