;; Sample EDN file for map area definitions. Includes mobs and items right now, maybe will include other
;; entities likes traps, NPCs, etc. in the future.

{:mobs       ({:template   :orc
                :components {:spatial #=(clodiku.components/map->Spatial {:pos       {:x 700 :y 200}
                                                                          :size      14
                                                                          :direction #=(clodiku.components/directions :west)})}
                :inventory  ()
                :equipment  {}})
 :free-items ()}