(defproject clodiku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.badlogicgames.gdx/gdx "1.4.1"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.4.1"]
                 [com.badlogicgames.ashley/ashley "1.3.1"]
                 [com.badlogicgames.gdx/gdx-platform "1.4.1"
                  :classifier "natives-desktop"]
                 [brute "0.3.0" :exclusions [org.clojure/clojure]]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :main clodiku.core
  :aot [clodiku.MyGame]
  :global-vars {*warn-on-reflection* true}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
