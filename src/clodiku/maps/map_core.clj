(ns clodiku.maps.map-core
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader)))

(defn load-map
  ([]
   (-> (TmxMapLoader.)
       (.load "./assets/maps/sample.tmx")))
  ([map-name]
   (-> (TmxMapLoader.)
       (.load (str "./assets/maps/" map-name ".tmx")))))