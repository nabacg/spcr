(defproject spcr "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [compojure "1.1.6" :exclusions [[org.clojure/clojure]
                                                 [ring/ring-core]]]
                 [ring "1.3.1"]
                 [ring-server "0.3.1" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring/ring-json "0.3.1"]
                 [com.novemberain/monger "2.0.0"]
                 [jarohen/nomad "0.7.0"]

                 ;;cljs
                 [org.clojure/clojurescript "0.0-2740"]
                 [reagent "0.4.2"]

                 [cljs-ajax "0.2.6"]
                 [json-html "0.2.3"]]

  :main ^:skip-aot spcr.core
  :local-repo "lib"
  :min-lein-version "2.0.0"
  :uberjar-name "spcr.jar"
  :plugins [[jarohen/lein-frodo "0.4.1"]
            [lein-cljsbuild "1.0.3"]]
  :frodo/config-resource "config.edn"

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/cljs"]
              :compiler {:output-dir "resources/public/js/out",
                        
                         :optimizations :whitespace,
                         :output-to "resources/public/js/app.js",
                         :source-map "resources/public/js/out.js.map",
                         :pretty-print true}}
             ]}
  :profiles {:uberjar {:aot :all}})
