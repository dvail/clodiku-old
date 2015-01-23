(ns clodiku.maps.map-core
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader TiledMap TiledMapTileLayer TiledMapTileLayer$Cell)
           (com.badlogic.gdx.math Vector3 Circle)
           (com.badlogic.gdx.graphics OrthographicCamera)
           (com.badlogic.gdx.maps MapProperties)
           (clodiku.components WorldMap))
  (:require [clodiku.util.entities :as eu]))

; TODO Pull this dynamically? Will the map tile size ever change?
(def tile-size 32)

(defn get-current-map
  "Get the reference to the TiledMap that the player is currently on"
  [system]
  (let [worldmap (eu/first-entity-with-comp system WorldMap)]
    (:tilemap (eu/comp-data system worldmap WorldMap))))

(defn get-current-map-grid
  "Get the 2d array representing walkable tiles on the map"
  [system]
  (let [worldmap (eu/first-entity-with-comp system WorldMap)]
    (:grid (eu/comp-data system worldmap WorldMap))))

(defn get-map-bounds
  "Gets the appropriate camera view of a tiled map based on the player location and map edges"
  [system camera]
  (let [player-pos ^Circle (eu/get-player-pos system)
        player-x (.x player-pos)
        player-y (.y player-pos)
        cam-width (/ (.viewportWidth ^OrthographicCamera camera) 2)
        cam-height (/ (.viewportHeight ^OrthographicCamera camera) 2)
        map-props (.getProperties ^TiledMap (get-current-map system))
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

; TODO This is inefficient, unreadable or some mix of the two...
;; Might be able to change this to a 2d array of 'shorts', but probably doesn't matter
(defn load-map-grid
  "Generates a matrix representing the passable and unpassable tiles on the grid for use in pathfinding.
  Tiles that are not walkable are represented with a -1. All other tiles have their traversal cost as some multiple
  of one, where one is the easiest to traverse."
  [^TiledMap tmxmap]
  (let [layer ^TiledMapTileLayer (.get (.getLayers tmxmap) 0)
        width (.getWidth layer)
        height (.getHeight layer)]
    (into-array
      (pmap (fn [row]
              (int-array (map (fn [col]
                                (if (= "false" (.get (->> ^TiledMapTileLayer$Cell (.getCell layer row col)
                                                          (.getTile)
                                                          (.getProperties)) "walkable"))
                                  -1 1))
                              (range 0 width)))) (range 0 height)))))

(defn load-map
  ([]
    (-> (TmxMapLoader.)
        (.load "./assets/maps/sample.tmx")))
  ([map-name]
    (-> (TmxMapLoader.)
        (.load (str "./assets/maps/" map-name ".tmx")))))
