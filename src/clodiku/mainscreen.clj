(ns clodiku.mainscreen
  (:import (com.badlogic.gdx Gdx Screen)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx.maps.tiled TmxMapLoader TiledMapRenderer TiledMap)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           ))

(require '[clodiku.maps.map-core :as maps])
(require '[brute.entity :as be])

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)

(def system (be/create-system))

(defn screen []
  (proxy [Screen] []
    (show []
      (def camera (OrthographicCamera. 400 400) )
      (def batch (SpriteBatch.))
      (def map-renderer (OrthogonalTiledMapRenderer. (^TiledMap maps/load-map) batch)))
    (render [delta]
      (doto (Gdx/gl)
        (.glClearColor 0 0 0.2 0.3)
        (.glClear GL20/GL_COLOR_BUFFER_BIT))
      (doto map-renderer
        (.setView camera)
        (.render)))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))