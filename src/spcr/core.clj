(ns spcr.core
  (:use [ring.middleware.reload]
        [ring.util.response])
  (:require [compojure.handler :as handler]
            [compojure.core
             :as c-core
             :refer [defroutes GET POST PUT DELETE HEAD OPTIONS PATCH ANY]]
            [compojure.route :as c-route]
            [ring.server.standalone :as server]
            [ring.middleware.json :as ring-json]
            [monger.core :as mg]
            [monger.collection :as mc]
            [spcr.parser :as parser :refer [file->dicts]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def test-data "../resources/data/PEB_ETF.csv")
(def testp "/home/cab/DEV/projects/spcr/resources/data/PEB_ETF.csv")

(defroutes endpoints
  (GET "/" [] (response {:key "Hello world"}))
  (GET "/data" [] (response (parser/file->dicts test-data))))




(def app
  (-> (var endpoints)
      (handler/api)
      (wrap-reload '(spcr.core))
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)))

(defn start-server [port]
  (server/serve #'app
                {:port port
                 :join? false
                 :open-browser? false}))


(defn -main
  "starting new server listening on port"
  [port]
  (start-server (Integer. port)))
