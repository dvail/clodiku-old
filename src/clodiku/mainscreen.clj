(ns clodiku.mainscreen
  (:require [clodiku.maps.map-core :as maps]
            [clodiku.systems.input :as system-input]
            [brute.entity :as be]
            [brute.system :as bs])
  (:import (com.badlogic.gdx Gdx Screen)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx.maps.tiled TmxMapLoader TiledMapRenderer TiledMap)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)))

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)

(declare system)

(defn- init-resources []
  (def camera (OrthographicCamera. 400 400))
  (def batch (SpriteBatch.))
  (def map-renderer (OrthogonalTiledMapRenderer. (^TiledMap maps/load-map) batch)))

(defn- init-systems
  "Register all the sub systems"
  []
 (def system (->  (be/create-system)
                    (bs/add-system-fn system-input/update))) )

(defn screen []
  (proxy [Screen] []
    (show []
      (init-resources)
      (init-systems) )
    (render [delta]
      (doto (Gdx/gl)
        (.glClearColor 0 0 0.2 0.3)
        (.glClear GL20/GL_COLOR_BUFFER_BIT))
      (doto map-renderer
        (.setView camera)
        (.render))
      (bs/process-one-game-tick system delta))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))
