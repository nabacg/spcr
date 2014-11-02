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
            [clojure.edn :as edn]
            [ring.server.standalone :as server]
            [ring.middleware.json :as ring-json]
            [spcr.parser :as parser]
            [spcr.mongodal :as db]
            [nomad :refer [defconfig]]
            [frodo.web :refer [App]]
            [frodo.core]))

(def test-data  (str
                 (clojure.string/replace
                  (-> (java.io.File. ".") .getAbsolutePath)
                  #"\."
                  "")
                 "PEB_ETF.csv"))

(defconfig app-config (io/resource "config.edn"))

(defn get-db-config []
  (println (app-config))
  (-> (:db-config (app-config))
      (assoc :uri (System/getenv "SPCR_MONGO_URL"))))


(defn abs [n] (max n (- n))) ;; move to Utils namespace

(def rule-predicate-forms {
                      :daily-gain '(fn [{open :Open close :Close}]
                                    (> close open))
                      :high-volume '(fn [{volume :Volume}]
                                     (> volume 50000.0))
                           :default '(fn [_] :true)})

;functions using other functions aren't allowed at the momment
(def default-rules { :default (fn [_] :true)

                    :high-daily-diff (fn [{high :High low :Low}]
                                         (> (abs (- high low)) 3))})

(defn get-matching-rules [record rules]
  (remove nil?
          (map (fn [[key pred-fn]]
                 (if (pred-fn record)
                   key
                   nil))
               rules)))

(defn rule-engine [data rules]
  (map
   (fn [row]
     (assoc row :labels (get-matching-rules row rules )))
   data))

(defn get-data []
  (->> (db/get-all "rawdata")
       (map #(dissoc % :_id))))

(defn get-rules []
  (->> (db/get-all "categoryrules")
       (map (fn [{name :name pred-fn :predicate}]
              [(keyword  name) (eval  (edn/read-string pred-fn))]))
       (into {})))

(defn get-labeled-data [] ;todo add a query mechanism to filter on labels
  (-> (get-data)
      (rule-engine (merge default-rules (get-rules)))))

(defn get-label-stats []
  (->> (get-labeled-data)
       (map :labels)
       (flatten)
       (group-by identity)
       (map (fn [[k v]] [k (count v)]))))

(defn import-file [file-path]
  (-> (parser/file->dicts file-path)
      (db/save "rawdata")))

(defroutes endpoints
  (GET "/" [] (slurp "resources/public/html/index.html"))
  (GET "/health" [] (response {:key "Hello world"}))
  (GET "/data" [] (response (get-data)))
  (GET "/label" [] (response (get-labeled-data)))
  (GET "/label-stats" [] (response (get-label-stats)))
  (mp/wrap-multipart-params
   (POST "/upload" {{{tempfile-path :tempfile} "file"} :multipart-params}
         (do
           (import-file tempfile-path)
           (response "Import successful! Go back to review your data"))))
  (c-route/resources "/"))


(defn prep-rules-for-saving [rules-list]
  (map
   (fn [[k v]] {:name  k  :predicate (pr-str v)})
   rules-list))

(defn init []
  (db/init (get-db-config))
  (if (nil? (seq (get-rules)))
    (db/save
     (prep-rules-for-saving rule-predicate-forms)
     "categoryrules"))
  (if (nil? (seq (get-data)))
    (import-file test-data)))

(def app
  (-> (var endpoints)
      (handler/api)
      (wrap-reload '(spcr.core))
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)))

(def frodo-app
  (reify App
    (start! [_]
      (do  (init)
           {:frodo/handler app}))
    (stop! [_ system]
      (println "Stopping app"))))

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
