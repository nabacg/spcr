(ns spcr.core
  (:use [ring.middleware.reload]
        [ring.util.response])
  (:require [compojure.handler :as handler]
            [compojure.core
             :as c-core
             :refer [defroutes GET POST PUT DELETE HEAD OPTIONS PATCH ANY]]
            (ring.middleware [multipart-params :as mp])
            [clojure.java.io :as io]
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

(defn category-stats []
  (->> (categorize)
       (map (fn [[key values]] [key (count values)]))
       (into {})))

(defn categorize []
  (->> (get-data)
       (filter #(< (read-string (:Close %)) 40))
       (group-by (fn [{close :Close low :Low high :High}]
                   (let [cat-num (mod (apply + (map read-string [close low high])) 10)]
                     (cond
                      (> cat-num 9) :very-high
                      (> cat-num 8.5) :high
                      (> cat-num 8) :medium
                      :default      :low))))))

(defn import-file [file-path]
  (-> (parser/file->dicts file-path)
      db/save))

(defroutes endpoints
  (GET "/" [] (slurp "resources/public/html/index.html"))
  (GET "/health" [] (response {:key "Hello world"}))
  (GET "/data" [] (response (get-data)))
  (GET "/label" [] (response (categorize)))
  (GET "/label-stats" [] (response (category-stats)))
  (mp/wrap-multipart-params
   (POST "/upload" {{{tempfile-path :tempfile} "file"} :multipart-params}
         (do
           (import-file tempfile-path)
           (response "Import successful!"))))
  (c-route/resources "/"))


(defn init []
  (if (nil? (seq (db/get-all)))
    (import-file test-data)))

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
