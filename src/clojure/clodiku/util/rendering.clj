(ns clodiku.util.rendering
  (:import (com.badlogic.gdx.graphics.g2d Animation$PlayMode Animation TextureAtlas$AtlasRegion TextureAtlas)))

; TODO This might need a more elegant/efficient/readable way of packing up entities...
(defn split-texture-pack
  "Returns a nested map where each top level key is the entities state. These keys map to
  a second level map with the keys representing a cardinal direction and the values are a looping
  animation of that state/direction combination."
  [atlas-location]
  (let [atlas (TextureAtlas. ^String atlas-location)
        regions (sort #(compare (.name ^TextureAtlas$AtlasRegion %1) (.name ^TextureAtlas$AtlasRegion %2)) (seq (.getRegions atlas)))
        action-map (map (fn [reg]
                          (let [splits (clojure.string/split (.name ^TextureAtlas$AtlasRegion reg) #"-")
                                action (keyword (first splits))
                                direction (keyword (second splits))]
                            {action {direction [reg]}})) regions)
        raw-map (apply merge-with (fn [first-val sec-val]
                                    (merge-with #(conj %1 (first %2)) first-val sec-val)) action-map)]
    (reduce-kv
      (fn [init fk fv]
        (assoc init fk
                    (apply merge
                           (map
                             (fn [dir-map]
                               (let [anim-speed (if (= fk :melee) 1/24 1/12)
                                     animation (Animation. (float anim-speed)
                                                           #^"[Lcom.badlogic.gdx.graphics.g2d.TextureRegion;" (into-array (val dir-map)))]
                                 (assoc {}
                                   (key dir-map)
                                   (doto animation
                                     (.setPlayMode Animation$PlayMode/LOOP))))) fv)))) {} raw-map)))
