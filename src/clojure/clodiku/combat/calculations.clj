(ns clodiku.combat.calculations
  (:require [clodiku.entities.util :as eu])
  (:import (clodiku.components Attribute)))

(defn- attr-bonus
  "Returns the bonus gained from a certain attribute."
  [attr]
  (/ (- attr 10) 2))

(defn attack-damage
  "Calculate the damage done from an attack."
  [system attacker defender]
  (let [attacker-weapon (eu/get-entity-weapon system attacker)
        attacker-str (:str (eu/comp-data system attacker Attribute))
        defender-armor (eu/get-entity-armor system defender)
        defender-vit (:vit (eu/comp-data system attacker Attribute))
        base-attack-damage (+ (attr-bonus attacker-str) (:base-damage attacker-weapon))
        base-damage-reduction (attr-bonus defender-vit)]
    (max 0 (- base-attack-damage base-damage-reduction))))