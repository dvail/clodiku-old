;; Sample EDN file for map area definitions.

; mobs are dumb entities that wander around and are fightable
{:mobs       ({:template   :orc
               :components {:spatial {:pos {:x 300 :y 100} :size 14 :direction :west}}
               :inventory  ()
               :equipment  {}}
               {:template   :orc
                :components {:spatial {:pos {:x 920 :y 100} :size 14 :direction :west}}
                :inventory  ()
                :equipment  {}}
               {:template   :orc
                :components {:spatial {:pos {:x 100 :y 200} :size 14 :direction :west}}
                :inventory  ()
                :equipment  {}}
               {:template   :orc
                :components {:spatial {:pos {:x 100 :y 500} :size 14 :direction :west}}
                :inventory  ()
                :equipment  {}}
               {:template   :orc
                :components {:spatial {:pos {:x 700 :y 200} :size 14 :direction :west}}
                :inventory  ()
                :equipment  {}})
 ; Free items are items that are just laying on the ground
 :free-items ({:template   :sword
               :components {:spatial   {:pos {:x 780 :y 680} :size 10}
                            :eq-weapon {:base-damage 25 :hit-box {:x 0 :y 0 :size :sword} :hit-list '() :type :sword}}})
 ; Containers are any items that cannot be picked up (?) and hold other items. chests, etc.
 :containers ()
 ; Triggers are interactive effects that occur at a certain time, location, more? Does this include traps?
 :triggers ()
 ; NPCs are entities with a bit more rpg "meat" to them compared to mobs
 :npcs ()}