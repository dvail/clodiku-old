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
(defrecord Player [data])

(def spatial-keys #{:pos :size})
(defrecord Spatial [data])

(def animated-keys #{:regions})
(defrecord Animated [data])

(def state-keys #{:current :time})
(defrecord State [data])

(def attribute-keys attributes)
(defrecord Attribute [data])

;equipable is a map that maps eq slots to items
;this component holds the total of all eq item stats for quick calculations
(def equipable-keys #{:equipment :stat-total})
(defrecord Equipable [data])

; EqItem is the actual component representing a piece of combat
(def eqitem-keys (union eq-stats #{:slot}))
(defrecord EqItem [data])

; A weapon component has a hit box that checks for collisions, as well as a function that describes the motion of
; an attack
(def eqweapon-keys #{:base-damage :hit-box :hit-list :type})
(defrecord EqWeapon [data])

(def mobai-keys #{:state :last-update :path})
(defrecord MobAI [data])


