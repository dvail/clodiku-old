(ns clodiku.maps.pathfinding
  (:require [clodiku.util.entities :as eu]
            [clodiku.maps.map-core :as maps]
            [clojure.data.priority-map :as prior-map])
  (:import (com.badlogic.gdx.math Circle)
           (clojure.data)))

;; TODO The size in pixels of a square tile w/h
;; Might be better to pull this number from the map at runtime...
(def tile-size 32)

(defn manhatten
  "Get the manhatten distance between two tiles"
  [[x1 y1] [x2 y2]]
  (+ (Math/abs ^Integer (- x2 x1)) (Math/abs ^Integer (- y2 y1))))

(defn walkable?
  "Check if the current tile in the grid is walkable. "
  [grid xy-map]
  (aget (aget grid (:x xy-map)) (:y xy-map)))

(defn find-path
  "Finds a path on the tiled map from A to B and returns a sequence of points
  representing that path"
  [system ^Circle start-loc ^Circle end-loc]
  (let [curr-tile {:x (Math/floor (/ (.x start-loc) tile-size)) :y (Math/floor (/ (.y start-loc) tile-size))}
        grid (maps/get-current-map-grid system)]
    (println (walkable? grid curr-tile))))
