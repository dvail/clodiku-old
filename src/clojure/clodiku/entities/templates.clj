(ns clodiku.entities.templates)

;;;
;;; TODO Move this out and possibly into some sort of data store???
;;;

(def item-templates {:sword {:components {:item       {:name        "A short sword"
                                                       :description "This short sword is dull"}
                                          :renderable {:texture "./assets/items/steel-sword.png"}
                                          :spatial    {:pos  {:x 0 :y 0}
                                                       :size 14}
                                          :eq-item    {:hr   1
                                                       :slot :held}
                                          :eq-weapon  {:base-damage 2
                                                       :hit-box     {:x 0 :y 0 :size :sword}
                                                       :hit-list    '()
                                                       :type        :sword}}}
                     :none  {:components {}}})

(def mob-templates {:orc  {:components {:state               {:current :walking
                                                              :time    0}
                                        :attribute           {:hp  30 :mp 5 :mv 50
                                                              :str 14 :dex 8 :vit 14 :psy 3}
                                        :spatial             {:pos       {:x 400 :y 400}
                                                              :size      14
                                                              :direction :west}
                                        :equipment           {:items {}}
                                        :skeletal-renderable {:scml "./assets/animation/humanoid/humanoid.scml"
                                                              :atlas "./assets/animation/humanoid/humanoid.pack"
                                                              :player nil
                                                              :loader nil
                                                              :drawer nil}
                                        :mob-ai              {:last-update 0
                                                              :state       :wander}}
                           :inventory  '()
                           :equipment  {:held {:template   :sword
                                               :components {}}}}
                    :none {:components {}}})
