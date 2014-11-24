(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx Graphics)
           (com.badlogic.gdx.graphics.g2d TextureAtlas))
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

(defn split-texture-pack
  "Returns a nested map representing each state an entity can be in and the direction
  is is facing and maps this information to a texture region."
  [atlas-location]
  (let [atlas (TextureAtlas. atlas-location)
        regions (sort #(compare (.name %1) (.name %2)) (seq (.getRegions atlas)))]
    (reduce (fn [map region]
              (let [splits (clojure.string/split (.name region) #"-")
                    stance (keyword (str (first splits) "-" (second splits)))]
                (if (contains? map stance)
                  (do
                    (assoc map stance (conj (stance map) region)))
                  (do
                    (assoc map stance [region]))))) {} regions)))

(defn init-resources! []
  (let [graphics Gdx/graphics]
    (def camera (OrthographicCamera.
                  (-> graphics (.getWidth))
                  (-> graphics (.getHeight))))
    (def batch (SpriteBatch.))
    (def map-renderer (OrthogonalTiledMapRenderer. (^TiledMap maps/load-map) batch))))

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
    (doto camera (.update))
    (doto camera-pos
      (.set ^Vector3 player-pos))
    (doto map-renderer
      (.setView camera)
      (.render)) system))