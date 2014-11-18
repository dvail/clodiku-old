(ns clodiku.MyGame
  (:gen-class
    :name clodiku.MyGame
    :extends com.badlogic.gdx.Game)
  (:import (com.badlogic.gdx Game)))

(require 'clodiku.mainscreen)
(refer 'clodiku.mainscreen)

(defn -create [^Game this]
  (.setScreen this (screen)))
