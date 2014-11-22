(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx Gdx)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (clodiku.components Player Position)
           (com.badlogic.gdx.math Vector3))
  (:require [clodiku.maps.map-core :as maps]
            [brute.entity :as be]))

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)

(defn init-resources! []
  (def camera (OrthographicCamera. 400 400))
  (def batch (SpriteBatch.))
  (def map-renderer (OrthogonalTiledMapRenderer. (^TiledMap maps/load-map) batch)))

(defn- get-player-pos
  [system]
  (let [player (first (be/get-all-entities-with-component system Player))
        pos (be/get-component system player Position)]
    (Vector3. (:x pos) (:y pos) 0)))

(defn render! [system delta]
  (let [camera-pos (-> camera (.position))
        player-pos (get-player-pos system)]
    (doto (Gdx/gl)
      (.glClearColor 0 0 0.2 0.3)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))
    (doto camera-pos
      (.set player-pos))
    (doto map-renderer
      (.setView camera)
      (.render)) system))