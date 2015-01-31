(ns spcr.parser
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

;;move to new utils namespace
(defn numeric? [s]
  (if-let [s (seq s)]
    (let [s (if (= (first s) \-) (next s) s)
          s (drop-while #(Character/isDigit %) s)
          s (if (= (first s) \.) (next s) s)
          s (drop-while #(Character/isDigit %) s)]
      (empty? s))))

(defn cast-numerics [str]
  (if (numeric? str)
    (Float. str)
    str))

(defn str->num [str]
  (Float. str))

(defn date? [str]
  (re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}" str))

(defn str->date [str]
  (.parse
      (java.text.SimpleDateFormat. "yyyy-MM-dd")
      str))

(defn try-cast [str]
  (cond
   (numeric? str)  (str->num str)
   (date? str) (str->date str)
   :default str))

(defn file->list [path]
  (with-open [input-file (io/reader path)]
    (doall
     (csv/read-csv input-file))))

(defn list->dicts [[header & body]]
  (let [keys (map keyword header)]
    (map #(zipmap keys (map try-cast %))
         body)))

(defn file->dicts [path]
  (list->dicts (file->list path)))
