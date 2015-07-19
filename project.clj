(defproject clodiku "0.1.0"
  :description "Clodiku: A top-down action rpg game inspired by LoZ: A Link to the Past and DikuMUD descendant Daedal Macabre"
  :url "http://dvail.com"
  :license {:name "License pending"
            :url "http://dvail.com"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.badlogicgames.gdx/gdx "1.6.4"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.6.4"]
                 [com.badlogicgames.gdx/gdx-platform "1.6.4"
                  :classifier "natives-desktop"]
                 [org.clojure/data.priority-map  "0.0.5"]
                 [brute "0.3.0" :exclusions [org.clojure/clojure]]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :main clodiku.core
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :aot [clodiku.entities.components clodiku.game clodiku.core ]
  :global-vars {*warn-on-reflection* true}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  )
