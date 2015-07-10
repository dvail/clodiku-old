(ns clodiku.systems.rendering
  (:import (com.badlogic.gdx.graphics.g2d TextureRegion)
           (clodiku.entities.components AnimatedRenderable State Spatial Attribute)
           (com.badlogic.gdx.math Circle)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx Gdx Graphics)
           (com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer)
           (com.badlogic.gdx.graphics.g2d SpriteBatch Animation BitmapFont)
           (com.badlogic.gdx.maps.tiled TiledMap)
           (com.badlogic.gdx.math Vector3)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer))
  (:require [clodiku.world.maps :as maps]
            [clojure.set :as cset]
            [clodiku.entities.util :as eu]))

(declare ^OrthographicCamera camera)
(declare ^SpriteBatch batch)
(declare ^OrthogonalTiledMapRenderer map-renderer)
(declare ^ShapeRenderer shape-renderer)
(declare ^BitmapFont attack-font)

(def map-background-layers (int-array 2 [0, 1]))
(def map-foreground-layers (int-array 1 [2]))

; TODO Generalize a way to get entities sharing multiple components
(defn get-animated-entities
  "Get all entities with both an Animated and a Spatial component"
  [system]
  (let [animated (eu/get-entities-with-components system AnimatedRenderable)
        spatial (eu/get-entities-with-components system Spatial)]
    (cset/intersection (set animated) (set spatial))))

(defn sort-entities-by-render-order
  "Sorts a collection of entities by 'y' value, so that entities closer
  to the bottom of the screen are drawn first"
  [system entities]
  (reverse (sort-by #(:y (:pos (eu/comp-data system % Spatial))) entities)))

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

(defn update-map
  "Swap the map to be rendered"
  [system map-name]
  (.setMap map-renderer (maps/get-current-map system))
  system)

(defn dorender
  "Renders a single entity"
  [entity batch system]
  (let [spatial (eu/comp-data system entity Spatial)
        state (eu/comp-data system entity State)
        region-map (:regions (eu/comp-data system entity AnimatedRenderable))
        pos (:pos spatial)
        region ^TextureRegion (.getKeyFrame ^Animation ((:direction spatial) ((:current state) region-map)) (:time state))]
    (doto ^SpriteBatch batch
      (.draw region
             ^float (- (:x pos) (/ (.getRegionWidth region) 2))
             ^float (- (:y pos) (:size spatial) -2)))))

(defn render-entities!
  "Render the player, mobs, npcs and items"
  [batch system]
  (let [entities (sort-entities-by-render-order system (get-animated-entities system))]
    (doseq [entity entities]
      (dorender entity batch system))))

(defn render-attack-verbs
  "Draw the *KICK POW BANG* verbs for attacks"
  ; TODO Probably will look better to do these as static images/animations rather than BitMap fonts
  [ batch _ events]
  (let [attacks (:combat (:world-events @events))]
    (doseq [attack attacks]
      (let [delta (:delta attack)
            draw-x  (float (:x (:location attack)))
            draw-y  (float (:y (:location attack)))]
        (.setColor attack-font 0.2 0.2 1 (- 1 (/ delta 2)))
        (.draw attack-font ^SpriteBatch batch "poke" draw-x (+ 25 draw-y (* 100 delta)))))))

(defn render-entity-shapes!
  "Render the actual spatial component of the entities"
  [renderer system]
  (let [entities (eu/get-entities-with-components system Spatial)
        spatials (map (fn [ent] (eu/comp-data system ent Spatial)) entities)]
    (doseq [space spatials]
      (doto ^ShapeRenderer renderer
        (.circle (:x (:pos space)) (:y (:pos space)) (:size space))))))

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


(defn render! [system delta events]
  (let [camera-pos (.position camera)]
    (doto (Gdx/gl)
      (.glClearColor 0 0 0.2 0.3)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))
    (doto camera (.update))
    (doto camera-pos
      (.set ^Vector3 (maps/get-map-bounds system camera)))
    (doto map-renderer
      (.setView camera)
      (.render map-background-layers))
    (doto batch
      (.begin)
      (.setProjectionMatrix (.combined camera))
      (render-entities! system)
      (render-attack-verbs system events)
      (.end))
    (doto shape-renderer
      (.setAutoShapeType true)
      (.setProjectionMatrix (.combined camera))
      (.begin)
      (.setColor 0.5 1 0.5 1)
      (render-entity-shapes! system)
      (.setColor 1 0.5 0.5 1)
      (render-attack-shapes! system)
      (.end))
    (doto map-renderer
      (.setView camera)
      (.render map-foreground-layers)) system))
