(ns clodiku.mainscreen
  (:import (com.badlogic.gdx Gdx Screen)))


(defn screen []
  (proxy [Screen] []
    (show [])
    (render [])
    (dispose[])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))