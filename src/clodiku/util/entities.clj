(ns clodiku.util.entities
  (:import (clodiku.components Player Spatial WorldMap))
  (:require [brute.entity :as be]))

(defn get-player-component
  "Get a named component type from the player"
  [system comp]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (be/get-component system player comp)))

(defn get-player-pos
  "Get the Spatial component of the player character"
  [system]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (:pos (be/get-component system player Spatial))))

(defn get-current-map
  "Get the reference to the TiledMap that the player is currently on"
  [system]
  (let [worldmap (first (be/get-all-entities-with-component system WorldMap))]
    (:tilemap (be/get-component system worldmap WorldMap))))