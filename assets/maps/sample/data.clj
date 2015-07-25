;; Sample EDN file for map area definitions. Includes mobs and items right now, maybe will include other
;; entities likes traps, NPCs, etc. in the future.

{:mobs       ({:template   :orc
               :components {:spatial {:pos       {:x 300 :y 100}
                                      :size      14
                                      :direction :west}}
               :inventory  ()
               :equipment  {}}

               {:template   :orc
                :components {:spatial {:pos       {:x 920 :y 100}
                                       :size      14
                                       :direction :west}}
                :inventory  ()
                :equipment  {}}

               {:template   :orc
                :components {:spatial {:pos       {:x 100 :y 200}
                                       :size      14
                                       :direction :west}}
                :inventory  ()
                :equipment  {}}

               {:template   :orc
                :components {:spatial {:pos       {:x 100 :y 500}
                                       :size      14
                                       :direction :west}}
                :inventory  ()
                :equipment  {}}

               {:template   :orc
                :components {:spatial {:pos       {:x 700 :y 200}
                                       :size      14
                                       :direction :west}}
                :inventory  ()
                :equipment  {}})
 :free-items ({:template   :sword
               :components {:spatial   {:pos  {:x 780 :y 680}
                                        :size 10}
                            :eq-weapon {:base-damage 25
                                        :hit-box     {:x 0 :y 0 :size :sword}
                                        :hit-list    '()
                                        :type        :sword}}})}