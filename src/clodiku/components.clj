(ns clodiku.components)

(def states #{:walking :standing :melee :casting :stunned :dead})
(def directions #{:east :west :north :south})
(def eq-slots #{:held :body :head :feet :hands})

(def attributes #{:hp :str :dex :vit :psy})
(def eq-stats #{:damage :hr :dr :ms :pd :saves})

(def mob-ai-states #{:wander :aggro})

(defrecord WorldMap [tilemap grid])

(defrecord Player [])

(defrecord Spatial [pos direction])
(defrecord Animated [regions])
(defrecord State [current time data])

(defrecord Attributes [attributes])

;equipment is a map that maps eq slots to items
(defrecord Equipable [equipment])

; EqItem is the actual component representing a piece of equipment
(defrecord EqItem [stats])

; A weapon component has a hit box that checks for collisions, as well as a function that describes the motion of
; an attack
(defrecord EqWeapon [base-damage hit-box hit-list type])

(defrecord MobAI [state data])


