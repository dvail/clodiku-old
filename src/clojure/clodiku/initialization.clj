(ns clodiku.initialization
  (:import (clodiku.entities.components WorldMap))
  (:require [brute.entity :as be]
            [clodiku.entities.loader :as el]
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
                                  :hit-box     {:x 0 :y 0 :size :spear}
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
        make-comps (fn [params] (map #(apply comps/construct %) params))
        bind-components (fn [entity comps sys] (reduce #(be/add-component %1 entity %2) sys comps))]
    (->> (reduce (fn [sys entity] (be/add-entity sys entity)) system (list weap armor player))
         (bind-components weap (make-comps weap-params))
         (bind-components armor (make-comps armor-params))
         (bind-components player (make-comps player-params)))))

(defn init-entities [system area-name]
  (let [area-data (->> (str map-asset-dir area-name map-data-file)
                       (slurp)
                       (read-string))
        item-comps-seq (map #(el/init-item-comps %) (:free-items area-data))
        mobs (:mobs area-data)
        sys-with-items (reduce #(el/bind-item %1 %2) system item-comps-seq)]
    (reduce #(el/init-mob %1 %2) sys-with-items mobs)))

(defn init-main
  [system]
  (-> system
      (init-player)
      (init-map "sample")
      (init-entities "sample")))

