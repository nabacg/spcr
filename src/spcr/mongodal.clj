(ns spcr.mongodal
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]))


(def collection-name "raw-data")

(defn db-save [db data]
  (mc/insert-batch
   db
   collection-name
   data))

(defn db-connect []
  (let [conn (mg/connect)
        db (mg/get-db conn "srcp-db")]
    db))

(defn db-get-all [db]
  (mc/find-maps db collection-name))


(defn get-all []
  (-> (db-connect)
      (db-get-all)))

(defn save [data]
  (-> (db-connect)
      (db-save data)))
