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

(defn file->list [path]
  (with-open [input-file (io/reader path)]
    (doall
     (csv/read-csv input-file))))

(defn list->dicts [[header & body]]
  (let [keys (map keyword header)]
    (map #(zipmap keys (map cast-numerics %))
         body)))

(defn file->dicts [path]
  (list->dicts (file->list path)))
