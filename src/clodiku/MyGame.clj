(ns clodiku.MyGame
  (:gen-class
    :name clodiku.MyGame
    :extends com.badlogic.gdx.Game)
  (:import (com.badlogic.gdx Game)))

(defn -create [^Game this]
  (.setScreen this (clodiku.mainscreen/screen)))
