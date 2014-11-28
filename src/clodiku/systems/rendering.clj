(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx Graphics)
           (com.badlogic.gdx.graphics.g2d TextureAtlas)
           (clodiku.components Player AnimationMap Spatial State))
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

; TODO This might need a more elegant/efficient way of packing up entities...
(defn split-texture-pack
  "Returns a nested map representing each state an entity can be in and the direction
  it is facing and maps this information to a texture region."
  [atlas-location]
  (let [atlas (TextureAtlas. atlas-location)
        regions (sort #(compare (.name %1) (.name %2)) (seq (.getRegions atlas)))
        action-map (map (fn [reg]
                          (let [splits (clojure.string/split (.name reg) #"-")
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
                          {(key dir-map) (Animation. (float 1/12) (into-array (val dir-map)))}) fv)))) {} raw-map)))

(defn init-resources! []
  (let [graphics Gdx/graphics]
    (def camera (OrthographicCamera.
                  (-> graphics (.getWidth))
                  (-> graphics (.getHeight))))
    (def batch (SpriteBatch.))
    (def map-renderer (OrthogonalTiledMapRenderer. (^TiledMap maps/load-map) batch))))

(defn- get-player-pos
  [system]
  (let [player (first (be/get-all-entities-with-component system Player))]
    (be/get-component system player Spatial)))

(defn render-entities!
  "Render the player, mobs, npc's and items"
  [batch system delta]
  (let [player (first (be/get-all-entities-with-component system Player))
        pos (get-player-pos system)
        state (be/get-component system player State)
        region-map (:regions (be/get-component system player AnimationMap))]
    (doto batch
      ; TODO Change `delta` here to the state-time of the entity's current state
      (println region-map)
      (.draw (.getKeyFrame (:east (:walking region-map)) (:time state)) (.x (:pos pos)) (.y (:pos pos))))))

(defn render! [system delta]
  (let [camera-pos (-> camera (.position))
        player-pos (get-player-pos system)]
    (doto (Gdx/gl)
      (.glClearColor 0 0 0.2 0.3)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))
    (doto camera (.update))
    (doto camera-pos
      (.set ^Vector3 (Vector3. (.x (:pos player-pos)) (.y (:pos player-pos)) 0)))
    (doto map-renderer
      (.setView camera)
      (.render))
    (doto batch
      (.begin)
      (.setProjectionMatrix (-> camera (.combined)))
      (render-entities! system delta)
      (.end)) system))