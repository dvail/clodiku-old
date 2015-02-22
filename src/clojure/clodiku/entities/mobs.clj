;;; This is a collection of functions that define mobs available in the game
;;; Each function should return a seqence of data components to be attached to an entity
(ns clodiku.entities.mobs
  (:require [clodiku.components :as comps]
            [clodiku.systems.rendering :as sys-rendering]
            [brute.entity :as be]))


;; TODO Reuse animation regions when possible
(defn make-orc
  [system]
  (println "Orc test")
  (let [anim-regions (sys-rendering/split-texture-pack "./assets/mob/orc/orc.pack")
        mob (be/create-entity)]
    {:equipment {:held :sword}}
    (-> system
        (be/add-entity mob)
        (be/add-component mob (comps/->Attribute {:hp  30
                            :mv  50
                            :str 14
                            :dex 8
                            :vit 14
                            :psy 3}))
        (be/add-component mob (comps/->MobAI {:state       (comps/mob-ai-states :wander)
                        :last-update 0
                        :path        '()}))
        (be/add-component mob (comps/->Animated {:regions anim-regions}))
        (be/add-component mob (comps/->Equipable {:equipment {}}))
        (be/add-component mob (comps/->State {:current (comps/states :walking)
                        :time    0.0}))
        (be/add-component mob (comps/->Spatial {:pos       {:x 300 :y 300}
                          :size      14
                          :direction (comps/directions :west)})))))

(def mob-factory-map {:orc make-orc})


; TODO This might be a good place for a macro??
(defn init-mob
  "Get a sequence of components based on a mob keyword"
  [system mob-type]
  ((get mob-factory-map mob-type) system))



