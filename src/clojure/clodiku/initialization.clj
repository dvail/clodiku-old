(ns clodiku.initialization
  (:import (com.badlogic.gdx.math Circle)
           (clodiku.entities.components WorldMap))
  (:require [brute.entity :as be]
            [clodiku.entities.mobs :as em]
            [clodiku.util.rendering :as rendering]
            [clodiku.combat.weaponry :as weaponry]
            [clodiku.world.maps :as maps]
            [clodiku.entities.util :as eu]
            [clodiku.entities.components :as comps]))


(def ^:const map-asset-dir "./assets/maps/")
(def ^:const map-data-file "/data.clj")

(defn init-map [sys map-name]
  (let [tmx-map (maps/load-map map-name)
        map-grid (maps/load-map-grid tmx-map)
        map-entity (eu/first-entity-with-comp sys WorldMap)]
    (if (nil? map-entity)
      (let [new-entity (be/create-entity)]
        (-> sys
            (be/add-entity new-entity)
            (be/add-component new-entity (comps/map->WorldMap {:tilemap tmx-map
                                                               :grid    map-grid}))))
      (-> sys
          (eu/comp-update map-entity WorldMap {:tilemap tmx-map
                                               :grid    map-grid})))))

(defn init-player [system]
  (let [player (be/create-entity)
        weap (be/create-entity)
        armor (be/create-entity)
        weap-params {:item       {:name        "An emerald spear"
                                  :description "This spear doesn't look very sharp"}
                     :spatial    {:pos  :carry
                                  :size 16}
                     :renderable {:texture "./assets/items/emerald-spear.png"}
                     :eq-item    {:hr   1
                                  :slot :held}
                     :eq-weapon  {:base-damage 5
                                  :hit-box     (Circle. (float 0) (float 0) (float (:spear weaponry/weapon-sizes)))
                                  :hit-list    '()
                                  :type        :spear}}
        armor-params {:item       {:name        "Silver armor"
                                   :description "This armor is made of silver"}
                      :spatial    {:pos  :carry
                                   :size 16}
                      :renderable {:texture "./assets/items/silver-scale-mail.png"}
                      :eq-item    {:ed   3
                                   :slot :body}
                      :eq-armor   {:bulk 2}}
        player-params {:player              {}
                       :attribute           {:hp  50 :mp 20 :mv 50
                                             :str 10 :dex 10 :vit 10 :psy 10}
                       :animated-renderable {:regions "./assets/player/player.pack"}
                       :state               {:current :walking
                                             :time    0.0}
                       :equipment           {:items {:held weap}}
                       :inventory           {:items (list armor)}
                       :spatial             {:pos       {:x 750 :y 660}
                                             :size      14
                                             :direction :east}}
        weap-comps (map #(apply comps/construct %1) weap-params)
        armor-comps (map #(apply comps/construct %1) armor-params)
        player-comps (map #(apply comps/construct %1) player-params)
        bind-components (fn [entity comps sys] (reduce #(be/add-component %1 entity %2) sys comps))]
    (->> (reduce (fn [sys entity] (be/add-entity sys entity)) system (list weap armor player))
         (bind-components weap weap-comps)
         (bind-components armor armor-comps)
         (bind-components player player-comps))))

(defn init-entities [system area-name]
  (binding [*read-eval* true]
    (let [area-data (->> (str map-asset-dir area-name map-data-file)
                         (slurp)
                         (read-string))
          items (:free-items area-data)
          mobs (:mobs area-data)]
      (reduce #(em/init-mob %1 %2) system mobs))))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-map "sample")
      (init-entities "sample")))

