(ns clodiku.maps.map-core
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader TiledMap)
           (clodiku.components Player Spatial WorldMap)
           (com.badlogic.gdx.math Vector3 Circle)
           (com.badlogic.gdx.graphics OrthographicCamera)
           (com.badlogic.gdx.maps MapProperties))
  (:require [brute.entity :as be]))

(defn get-player-pos
  [system]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (be/get-component system player Spatial)))

(defn get-map-bounds
  "Gets the appropriate camera view of a tiled map based on the player location and map edges"
  [system camera]
  (let [player-pos ^Circle (:pos (get-player-pos system))
        player-x (.x player-pos)
        player-y (.y player-pos)
        tilemap (first (be/get-all-entities-with-component system WorldMap))
        cam-width (/ (.viewportWidth ^OrthographicCamera camera) 2)
        cam-height (/ (.viewportHeight ^OrthographicCamera camera) 2)
        map-props (.getProperties ^TiledMap (:tilemap (be/get-component system tilemap WorldMap)))
        map-width (* (.get ^MapProperties map-props "width" Integer) (.get ^MapProperties map-props "tilewidth" Integer))
        map-height (* (.get ^MapProperties map-props "height" Integer) (.get ^MapProperties map-props "tileheight" Integer))
        pos-x (cond
                (<= (- player-x cam-width) 0) cam-width
                (>= (+ player-x cam-width) map-width) (- map-width cam-width)
                true player-x)
        pos-y (cond
                (<= (- player-y cam-height) 0) cam-height
                (>= (+ player-y cam-height) map-height) (- map-height cam-height)
                true player-y)]
    (Vector3. pos-x pos-y 0)))

(defn load-map
  ([]
   (-> (TmxMapLoader.)
       (.load "./assets/maps/sample.tmx")))
  ([map-name]
   (-> (TmxMapLoader.)
       (.load (str "./assets/maps/" map-name ".tmx")))))