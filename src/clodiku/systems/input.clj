(ns clodiku.systems.input
  (:import (com.badlogic.gdx Gdx Input$Keys Input)))

(def bound-keys {:s Input$Keys/S :w Input$Keys/W :a Input$Keys/A :d Input$Keys/D})

(defn keys-pressed []
  (filter #(-> Gdx/input (.isKeyPressed (val %))) bound-keys))

(defn update [system delta]
  (println (keys-pressed))
  system)