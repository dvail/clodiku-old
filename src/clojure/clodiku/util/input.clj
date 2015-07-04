(ns clodiku.util.input
  (:import (com.badlogic.gdx Gdx Input$Keys)))

(def bound-keys {:move-south   Input$Keys/S
                 :move-north   Input$Keys/W
                 :move-west    Input$Keys/A
                 :move-east    Input$Keys/D
                 :melee-attack Input$Keys/P
                 :toggle-menus Input$Keys/TAB})

(defn pressed? [k]
  (-> Gdx/input (.isKeyPressed (k bound-keys))))


(defn just-pressed? [k]
  (-> Gdx/input (.isKeyJustPressed (k bound-keys))))
