(ns spcr.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [POST GET]]
            [clojure.string :refer [blank? join]]
            [json-html.core :refer [edn->hiccup]]))

(enable-console-print!)



(def state (atom {:raw-data []
                  :filter nil
                  :data-view []}))

(defn get-data []
  (GET "/label"
       {
        :format :json
        :handler (fn [raw-data]
                   (do  (swap! state #(-> %
                                          (assoc :raw-data raw-data)
                                          (assoc :data-view raw-data)))
                        (comment)  (.setTimeout js/window #(.DataTable (js/$ "#main-table"))
                                                300)))}))
(defn list-items [items]
  [:p (join ", " items)])

(defn table-row [headers row]
  [:tr
   (for [h headers]
     (do
       [:td (if (coll? (row h))
                    (list-items (row h))
                    (str  (row h)))]))])

(defn draw-table [data]
  (let [headers (keys (first data))]
    [:table#main-table.table.table-hover.table-bordered {:cell-spacing "0" :width "100%"}
     [:thead
      [:tr
       (for [header headers]
         [:th header])]]
     [:tbody
      (for [row data]
        [table-row headers row])]]))

(defn draw-list []
  [:div.col-sm-12
   (draw-table (:data-view @state))])

(defn string-contains? [value pattern]
  (> (.indexOf (str value) pattern) -1))

(defn match-on-all-columns [filter-str]
  (fn [row]
    (some (fn [cell]
            (if (coll? cell)
              (some #(string-contains? % filter-str) cell)
              (string-contains? cell filter-str)))
          (vals row))))

(defn match-on-label [filter-str]
  (fn [{labels "labels" :as row}]
    (some #(string-contains? % filter-str) labels)))

(defn handle-filter [filter-str]
  (let [raw-data (:raw-data @state)
        filtered-data (if (not (blank? filter-str))
                        (filter (match-on-all-columns filter-str) raw-data)
                        raw-data)]
    (swap! state #(-> %
                      (assoc :filter filter-str)
                      (assoc :data-view filtered-data)))))

(defn home-mounted []
  (.ready (js/$ js/document)
          (fn [] (.DataTable (js/$ "#main-table")))))


(defn home []

  [:div
   [:div.row
    [draw-list]]])


(defn home-component []
  (reagent/create-class {:component-function home
                         :component-did-mount home-mounted}))

(reagent/render-component [home] (.getElementById js/document "app"))
(get-data)
