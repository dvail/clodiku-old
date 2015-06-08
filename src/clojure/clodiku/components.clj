(ns clodiku.components
  (:require [clojure.set :refer [union]]))

(def states #{:walking :standing :melee :casting :stunned :dead})
(def directions #{:east :west :north :south})
(def eq-slots #{:held :body :head :feet :hands})

(def stats #{:hp :mv})
(def attributes #{:str :dex :vit :psy})
(def eq-stats #{:damage :hr :dr :ms :pd :saves})

(def mob-ai-states #{:wander :aggro})

(defrecord WorldMap [data])

; Nothing is stored in Player data at this point
(defrecord Player [])

(defrecord Spatial [pos size])

(defrecord AnimatedRenderable [regions])

(defrecord State [current time])

(defrecord Attribute [hp mp str dex vit psy])

;A component for entities that can have stuff!
(defrecord Inventory [items])

;equipable is a map that world eq slots to items
;this component holds the total of all eq item stats for quick calculations
(defrecord Equipable [equipment stat-total])

; EqItem is the actual component representing a combat item
(defrecord EqItem [slot damage hr dr ms pd saves])

; A weapon component has a hit box that checks for collisions, as well as a function that describes the motion of
; an attack
(defrecord EqWeapon [base-damage hit-box hit-list type])

; TODO Need to better define properties of armor, and all eq for that matter
(defrecord EqArmor [bulk])

(defrecord MobAI [state last-update path])


