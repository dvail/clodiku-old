(ns clodiku.equipment.weaponry
  (:import (com.badlogic.gdx.math Circle Vector2)))

(def attack-fns {:spear (fn [hit-box facing delta]
                          (let [rate 2
                                new-hit-vector (cond
                                                 (= facing :north) (Vector2. (.x hit-box) (+ (.y hit-box) rate))
                                                 (= facing :east) (Vector2. (+ (.x hit-box) rate) (.y hit-box))
                                                 (= facing :south) (Vector2. (.x hit-box) (- (.y hit-box) rate))
                                                 (= facing :west) (Vector2. (- (.x hit-box) rate) (.y hit-box)))]
                            (Circle. (.x new-hit-vector) (.y new-hit-vector) (.radius hit-box))))})
