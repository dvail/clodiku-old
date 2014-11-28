(ns clodiku.components)

(def states #{:walking :standing :melee :dead})
(def directions #{:east :west :north :south})

(defrecord Player [])

(defrecord Spatial [pos direction])

(defrecord AnimationMap [regions])

(defrecord State [current time])