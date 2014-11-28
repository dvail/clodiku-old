(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx Graphics)
           (com.badlogic.gdx.graphics.g2d TextureAtlas Animation$PlayMode TextureRegion TextureAtlas$AtlasRegion)
           (clodiku.components Player AnimationMap Spatial State)
           (com.badlogic.gdx.math Circle))
  (:import (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx Gdx)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.graphics.g2d SpriteBatch Animation)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (com.badlogic.gdx.math Vector3))
  (:require [clodiku.maps.map-core :as maps]
            [brute.entity :as be]))

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)

; TODO This might need a more elegant/efficient/readable way of packing up entities...
(defn split-texture-pack
  "Returns a nested map where each top level key is the entities state. These keys map to
  a second level map with the keys representing a cardinal direction and the values are a looping
  animation of that state/direction combination."
  [atlas-location]
  (let [atlas (TextureAtlas. ^String atlas-location)
        regions (sort #(compare (.name ^TextureAtlas$AtlasRegion %1) (.name ^TextureAtlas$AtlasRegion %2)) (seq (.getRegions atlas)))
        action-map (map (fn [reg]
                          (let [splits (clojure.string/split (.name ^TextureAtlas$AtlasRegion reg) #"-")
                                action (keyword (first splits))
                                direction (keyword (second splits))]
                            {action {direction [reg]}})) regions)
        raw-map (apply merge-with (fn [first-val sec-val]
                                    (merge-with #(conj %1 (first %2)) first-val sec-val)) action-map)]
    (reduce-kv
      (fn [init fk fv]
        (assoc init fk
               (apply merge
                      (map
                        (fn [dir-map]
                          (let [animation (Animation. (float 1/12) (into-array (val dir-map)))]
                            (assoc {}
                                   (key dir-map)
                                   (doto animation
                                     (.setPlayMode Animation$PlayMode/LOOP))))) fv)))) {} raw-map)))

(defn init-resources!
  [system]
  (let [graphics Gdx/graphics
        map-entity (first (be/get-all-entities-with-component system TiledMap))]
    (def camera (OrthographicCamera.
                  (.getWidth graphics)
                  (.getHeight graphics)))
    (def batch (SpriteBatch.))
    (def map-renderer
      (OrthogonalTiledMapRenderer.
        (be/get-component system map-entity TiledMap) batch))))


(defn render-entities!
  "Render the player, mobs, npcs and items"
  [batch system]
  (let [player (first (be/get-all-entities-with-component system Player))
        pos (clodiku.maps.map-core/get-player-pos system)
        state (be/get-component system player State)
        region-map (:regions (be/get-component system player AnimationMap))]
    (doto ^SpriteBatch batch
      (.draw ^TextureRegion
             (.getKeyFrame ^Animation ((:direction pos) ((:current state) region-map)) (:time state))
             (.x ^Circle (:pos pos))
             (.y ^Circle (:pos pos))))))

(defn render! [system delta]
  (let [camera-pos (.position camera)]
    (doto (Gdx/gl)
      (.glClearColor 0 0 0.2 0.3)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))
    (doto camera (.update))
    (doto camera-pos
      (.set ^Vector3 (clodiku.maps.map-core/get-map-bounds system camera)))
    (doto map-renderer
      (.setView camera)
      (.render))
    (doto batch
      (.begin)
      (.setProjectionMatrix (.combined camera))
      (render-entities! system)
      (.end)) system))