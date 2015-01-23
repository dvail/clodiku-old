(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx.graphics.g2d TextureAtlas Animation$PlayMode TextureRegion TextureAtlas$AtlasRegion)
           (clodiku.components Animated State Spatial)
           (com.badlogic.gdx.math Circle)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx Gdx Graphics)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.graphics.g2d SpriteBatch Animation BitmapFont)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (com.badlogic.gdx.math Vector3)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer))
  (:require [clodiku.maps.map-core :as maps]
            [brute.entity :as be]
            [clojure.set :as cset]
            [clodiku.util.entities :as eu]))

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)
(declare ^ShapeRenderer shape-renderer)
(declare ^BitmapFont attack-font)

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
                               (let [anim-speed (if (= fk :melee) 1/24 1/12)
                                     animation (Animation. (float anim-speed) (into-array (val dir-map)))]
                                 (assoc {}
                                   (key dir-map)
                                   (doto animation
                                     (.setPlayMode Animation$PlayMode/LOOP))))) fv)))) {} raw-map)))

; TODO Generalize a way to get entities sharing multiple components
(defn get-animated-entities
  "Get all entities with both an Animated and a Spatial component"
  [system]
  (let [animated (be/get-all-entities-with-component system Animated)
        spatial (be/get-all-entities-with-component system Spatial)]
    (cset/intersection (set animated) (set spatial))))

(defn sort-entities-by-render-order
  "Sorts a collection of entities by 'y' value, so that entities closer
  to the bottom of the screen are drawn first"
  [system entities]
  (reverse (sort-by #(.y ^Circle (:pos (be/get-component system % Spatial))) entities)))

(defn init-resources!
  [system]
  (let [graphics ^Graphics Gdx/graphics]
    (def camera (OrthographicCamera.
                  (.getWidth graphics)
                  (.getHeight graphics)))
    (def batch (SpriteBatch.))
    (def attack-font (BitmapFont.))
    (def map-renderer
      (OrthogonalTiledMapRenderer.
        ^TiledMap (maps/get-current-map @system) batch))
    (def shape-renderer (ShapeRenderer.))))

(defn dorender
  "Renders a single entity"
  [entity batch system]
  (let [pos (be/get-component system entity Spatial)
        state (be/get-component system entity State)
        region-map (:regions (be/get-component system entity Animated))
        circle ^Circle (:pos pos)
        region ^TextureRegion (.getKeyFrame ^Animation ((:direction pos) ((:current state) region-map)) (:time state))]
    (doto ^SpriteBatch batch
      (.draw region
             (- (.x circle) (/ (.getRegionWidth region) 2))
             (- (.y circle) (.radius circle) -2)))))

(defn render-entities!
  "Render the player, mobs, npcs and items"
  [batch system]
  (let [entities (sort-entities-by-render-order system (get-animated-entities system))]
    (doseq [entity entities]
      (dorender entity batch system))))

(defn render-attack-verbs
  "Draw the *KICK POW BANG* verbs for attacks"
  ; TODO Probably will look better to do these as static images/animations rather than BitMap fonts
  [batch system]
  (let [attacks (:combat (:world_events system))]
    (doseq [attack attacks]
      (let [delta (:delta attack)
            draw-x (.x (:location attack))
            draw-y (.y (:location attack))]
        (.setColor attack-font 0.2 0.2 1 (- 1 (/ delta 2)))
        (.draw attack-font batch "poke" draw-x (+ 25 draw-y (* 100 delta)))))))

(defn render-entity-shapes!
  "Render the actual spatial component of the entities"
  [renderer system]
  (let [entities (be/get-all-entities-with-component system Spatial)
        circles (map (fn [ent] (:pos (be/get-component system ent Spatial))) entities)]
    (doseq [circle circles]
      (doto ^ShapeRenderer renderer
        (.circle (.x ^Circle circle) (.y ^Circle circle) (.radius ^Circle circle))))))


(defn render-attack-shapes!
  "Render the collision zones for entity attacks"
  [renderer system]
  (let [attackers (eu/get-attackers system)
        circles (map (fn [ent] (:hit-box (eu/get-entity-weapon system ent))) attackers)]
    (doseq [circle circles]
      (doto ^ShapeRenderer renderer
        (.circle (.x ^Circle circle)
                 (.y ^Circle circle)
                 (.radius ^Circle circle))))))


(defn render! [system delta]
  (let [camera-pos (.position camera)]
    (doto (Gdx/gl)
      (.glClearColor 0 0 0.2 0.3)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))
    (doto camera (.update))
    (doto camera-pos
      (.set ^Vector3 (maps/get-map-bounds system camera)))
    (doto map-renderer
      (.setView camera)
      (.render))
    (doto batch
      (.begin)
      (.setProjectionMatrix (.combined camera))
      (render-entities! system)
      (render-attack-verbs system)
      (.end))
    (doto shape-renderer
      (.setAutoShapeType true)
      (.setProjectionMatrix (.combined camera))
      (.begin)
      (.setColor 0.5 1 0.5 1)
      (render-entity-shapes! system)
      (.setColor 1 0.5 0.5 1)
      (render-attack-shapes! system)
      (.end)) system))