(ns clodiku.equipment.weaponry
  (:import (com.badlogic.gdx.math Circle Vector2)))

(def weapon-types #{:spear :sword :club})

(def weapon-sizes {:spear 4
                   :sword 4
                   :club  10})

(def weapon-ranges {:spear 50
                    :sword 28
                    :club  25})

(def attack-start-pos {:spear (fn [pos facing]
                                (let [start-range (/ (.radius pos) 2)
                                      radius (:spear weapon-sizes)]
                                  (cond
                                    (= facing :north) (Circle. (.x pos) (+ (.y pos) start-range radius) radius)
                                    (= facing :south) (Circle. (.x pos) (- (.y pos) start-range radius) radius)
                                    (= facing :east) (Circle. (+ (.x pos) start-range radius) (.y pos) radius)
                                    (= facing :west) (Circle. (- (.x pos) start-range radius) (.y pos) radius))))
                       :sword (fn [pos facing]
                                (let [start-range (+ (:sword weapon-ranges) (/ (.radius pos) 2))
                                      radius (:sword weapon-sizes)]
                                  (cond
                                    (= facing :north) (Circle. (+ (.x pos) start-range radius) (.y pos) radius)
                                    (= facing :south) (Circle. (- (.x pos) start-range radius) (.y pos) radius)
                                    (= facing :east) (Circle. (.x pos) (- (.y pos) start-range radius) radius)
                                    (= facing :west) (Circle. (.x pos) (+ (.y pos) start-range radius) radius))))})

; Hit-box update functions for weapon types
; TODO Would be cool to memoize these, but need a way to limit the cache
(def attack-fns {:spear (fn [hit-box entity-space]
                          (let [rate 2
                                facing (:direction entity-space)
                                new-hit-vector (cond
                                                 (= facing :north) (Vector2. (.x hit-box) (+ (.y hit-box) rate))
                                                 (= facing :south) (Vector2. (.x hit-box) (- (.y hit-box) rate))
                                                 (= facing :east) (Vector2. (+ (.x hit-box) rate) (.y hit-box))
                                                 (= facing :west) (Vector2. (- (.x hit-box) rate) (.y hit-box)))]
                            (Circle. (.x new-hit-vector) (.y new-hit-vector) (.radius hit-box))))
                 :sword (fn [hit-box entity-space]
                          (let [rate 8                      ; rate affects the speed of swing and the distance of arc
                                entity-origin (:pos entity-space)
                                angle (Math/atan2
                                        (- (.x entity-origin) (.x hit-box))
                                        (- (.y entity-origin) (.y hit-box)))
                                new-x (* rate (Math/cos angle))
                                new-y (* rate (Math/sin angle))
                                new-hit-vector (Vector2. new-x new-y)]
                            (Circle. (+ (.x hit-box) (.x new-hit-vector)) (- (.y hit-box) (.y new-hit-vector)) (.radius hit-box))))})

(defn get-attack-fn
  "Get the attack function of a given weapon type's hit-box"
  [weapon-type]
  (weapon-type attack-fns))

(defn get-attack-start-pos
  "Gets the starting attack position for a given weapon type."
  [weapon-type entity-space]
  ((weapon-type attack-start-pos) (:pos entity-space) (:direction entity-space)))