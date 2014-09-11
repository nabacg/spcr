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
            [spcr.parser :as parser]
            [spcr.mongodal :as db]))

(def test-data  (str
                 (clojure.string/replace
                  (-> (java.io.File. ".") .getAbsolutePath)
                  #"\."
                  "")
                 "PEB_ETF.csv"))

(defn get-data []
  (->> (db/get-all)
       (map #(dissoc % :_id))))


(defroutes endpoints
  (GET "/" [] (response {:key "Hello world"}))
  (GET "/data" [] (response (get-data))))




(defn load-test-data [file-path]
  (-> (parser/file->dicts file-path)
      db/save))

(defn init []
  (if (nil? (seq (db/get-all)))
    (load-test-data test-data)))

(def app
  (-> (var endpoints)
      (handler/api)
      (wrap-reload '(spcr.core))
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)))

(defn start-server [port]
  (init)
  (server/serve #'app
                {:port port
                 :join? false
                 :open-browser? false}))


(defn -main

  "starting new server listening on port"
  [port]
  (start-server (Integer. port)))
