(ns clodiku.MyGame
  (:import (com.badlogic.gdx Game)))

(gen-class
  :name clodiku.MyGame
  :extends Game)

(defn -create [^Game this]
  (.setScreen this (clodiku.mainscreen/screen)))
