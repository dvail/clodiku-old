(ns clodiku.entities.components
  (:require [clojure.set :refer [union]]
            [clodiku.util.rendering :as r-util]
            [clodiku.combat.weaponry :as weaponry])
  (:import (com.badlogic.gdx.math Circle)))

(def ^:const states #{:walking :standing :melee :casting :stunned :dead})
(def ^:const directions #{:east :west :north :south})
(def ^:const eq-slots #{:held :body :head :feet :hands})

(def ^:const stats #{:hp :mv})
(def ^:const attributes #{:str :dex :vit :psy})
(def ^:const eq-stats #{:damage :hr :dr :ms :pd :saves})

(def ^:const mob-ai-states #{:wander :aggro})

;;;
;;; Component definitions
;;;

(defrecord WorldMap [data])

; Nothing is stored in Player data at this point
(defrecord Player [])

(defrecord Spatial [pos size])

(defrecord Renderable [texture])
(defrecord AnimatedRenderable [regions])

(defrecord State [current time])

(defrecord Attribute [hp mp str dex vit psy])

;equipable is a map that world eq slots to items
;this component holds the total of all eq item stats for quick calculations
(defrecord Equipment [items stat-total])

;A component for entities that can have stuff!
(defrecord Inventory [items])

;A component for all basic item types
(defrecord Item [name description])

; EqItem is the actual component representing a combat item
(defrecord EqItem [slot damage hr dr ms pd saves])

; A weapon component has a hit box that checks for collisions, as well as a function that describes the motion of
; an attack
(defrecord EqWeapon [base-damage hit-box hit-list type])

; TODO Need to better define properties of armor, and all eq for that matter
(defrecord EqArmor [bulk])

(defrecord MobAI [state last-update path])

;;;
;;; Component construction sugar
;;;

(def ^:const construct-fns {:worldmap            map->WorldMap
                            :player              map->Player
                            :spatial             map->Spatial
                            :renderable          map->Renderable
                            :animated-renderable map->AnimatedRenderable
                            :state               map->State
                            :attribute           map->Attribute
                            :equipment           map->Equipment
                            :inventory           map->Inventory
                            :item                map->Item
                            :eq-item             map->EqItem
                            :eq-weapon           map->EqWeapon
                            :eq-armor            map->EqArmor
                            :mob-ai              map->MobAI})

(defmulti construct "Construct a component given a key name and attribute map" (fn [key _] key))

(defmethod construct :renderable [_ val]
  (map->Renderable (->> (:texture val)
                        (r-util/make-texture)
                        (assoc val :texture))))

(defmethod construct :animated-renderable [_ val]
  (map->AnimatedRenderable (->> (:regions val)
                                (r-util/split-texture-pack)
                                (assoc val :regions))))

(defmethod construct :eq-weapon [_ val]
  (map->EqWeapon (let [{:keys [x y size]} (:hit-box val)
                       size (if (keyword? size) (size weaponry/weapon-sizes) size)]
                   (assoc val :hit-box (Circle. (float x) (float y) (float size))))))

(defmethod construct :default [key val]
  ((key construct-fns) val))

(defn construct-map
  "Takes a map of component definitions and returns a map of constructed components"
  [comp-defs]
  (reduce-kv #(assoc %1 %2 (construct %2 %3)) {} comp-defs))