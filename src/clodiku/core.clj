(ns clodiku.core
  (:gen-class)
  (:import (com.badlogic.gdx.backends.lwjgl LwjglApplication)
           (org.lwjgl.input Keyboard)))

(defn -main
  []
  (LwjglApplication. (clodiku.MyGame. ) "test" 400 300)
  (Keyboard/enableRepeatEvents true))
