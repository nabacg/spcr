(ns spcr.mongodal
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def db-config (atom nil))

(defn init [config]
  (reset! db-config config))

(defn db-save [db collection-name data]
  (mc/insert-batch
   db
   collection-name
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

(defn db-get-collection [db collection-name]
  (mc/find-maps db collection-name))

(defn get-all [name]
  (->
   (db-connect)
   (db-get-collection name)))

(defn save [data name]
  (-> (db-connect)
      (db-save name data)))
