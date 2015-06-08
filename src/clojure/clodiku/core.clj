(ns clodiku.core
  (:gen-class)
  (:import (com.badlogic.gdx.backends.lwjgl LwjglApplication)
           (org.lwjgl.input Keyboard)
           (clodiku game)))

(defn -main
  []
  (LwjglApplication. (game.) "Clodiku" 500 400)
  (Keyboard/enableRepeatEvents true))
