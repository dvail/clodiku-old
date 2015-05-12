(ns clodiku.world.transporter
  (:require [clodiku.initialization :as init]
            [clodiku.systems.rendering :as sys-rendering]
            [clodiku.entities.util :as eu]
            [clodiku.world.maps :as maps]
            [brute.entity :as be])
  (:import (com.badlogic.gdx.maps.objects RectangleMapObject)
           (clodiku.components Player Spatial MobAI)))

; TODO Break this function down, it is looking a little dense
(defn swap-areas
  "Transitions the player to a new area, replacing the map and relevant entities."
  [system ^RectangleMapObject transport-object]
  (let [transport-props (.getProperties transport-object)
        new-area (.get transport-props "area-name")
        tile-x (Integer/parseInt (.get transport-props "tile-x"))
        tile-y (Integer/parseInt (.get transport-props "tile-y"))
        player (eu/first-entity-with-comp system Player)]
    (-> system
        (eu/destroy-non-player-entities)
        ; TODO Destroy enemy eq/inv entities as well here.
        (init/init-map new-area)
        (eu/comp-update player Spatial {:pos (maps/tile-to-pixel system tile-x tile-y)})
        (sys-rendering/update-map new-area)
        (init/init-entities new-area))))
