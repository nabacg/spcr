(ns spcr.mongodal
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def db-config (atom nil))
                                        ;{:collection-name "rawdata" :db-name "spcr-db" :uri nil} ;:uri is optional if wanna connect via uri for Heroku, else connects to localhost
(defn init [config]
  (reset! db-config config))

(defn db-save [db data]
  (mc/insert-batch
   db
   (:collection-name @db-config)
   data))

(defn db-connect-via-uri [db-name uri]
  (let [{:keys [conn db]} (mg/connect-via-uri uri)]
    db))

(defn db-connect-localhost [db-name]
  (-> (mg/connect)
      (mg/get-db db-name)))

(defn db-connect []
  (let [{db-name :db-name uri :uri} @db-config]
    (if (nil? uri)
      (db-connect-localhost db-name)
      (db-connect-via-uri db-name uri))))


(defn db-get-all [db]
  (mc/find-maps db (:collection-name @db-config)))

(defn get-all []
  (println "!!!!!!" @db-config)
  (-> (db-connect)
      (db-get-all)))

(defn save [data]
  (-> (db-connect)
      (db-save data)))
