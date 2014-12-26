(ns clodiku.components)

(def states #{:walking :standing :melee :casting :stunned :dead})
(def directions #{:east :west :north :south})

(defrecord WorldMap [tilemap])

(defrecord Player [])

(defrecord Spatial [pos direction])
(defrecord Animated [regions])
(defrecord State [current time data])

(defrecord MobAI [])


