(ns clodiku.mainscreen
  (:import (com.badlogic.gdx Gdx Screen)
           (com.badlogic.gdx.graphics GL20)))

(defn screen []
  (proxy [Screen] []
    (show [])
    (render [delta]
      (doto (Gdx/gl)
        (.glClearColor 0 0 0.2 0.3)
        (.glClear GL20/GL_COLOR_BUFFER_BIT)))
    (dispose[])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))