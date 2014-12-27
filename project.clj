(defproject clodiku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.badlogicgames.gdx/gdx "1.5.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.5.0"]
                 [com.badlogicgames.gdx/gdx-platform "1.5.0"
                  :classifier "natives-desktop"]
                 [brute "0.3.0" :exclusions [org.clojure/clojure]]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
;  :plugins [[cider/cider-nrepl "0.8.1"]]
  :main clodiku.core
  :aot [clodiku.game clodiku.core ]
  :global-vars {*warn-on-reflection* true}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
