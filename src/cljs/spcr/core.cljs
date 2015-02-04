(ns spcr.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [POST GET]]
            [clojure.string :refer [blank? join]]
            [json-html.core :refer [edn->hiccup]]))

(enable-console-print!)



(def state (atom {:raw-data []
                  :filter nil
                  :data-view []}))

(comment
  (add-watch state :raw-data (fn [key ref old-value new-value]
                               (.setTimeout js/window #(.DataTable (js/$ "#main-table"))
                                          300))))

(defn get-data []
  (GET "/label"
       {
        :format :json
        :handler (fn [raw-data]
                   (do  (swap! state #(-> %
                                          (assoc :raw-data raw-data)
                                          (assoc :data-view raw-data)))
                        (.setTimeout js/window #(.DataTable (js/$ "#main-table"))
                                          300)))}))
(comment
  (defn list-items [lst]
    [:ul.list-group
     (for [i lst]
       [:li.list-group-item
        (str i)
        (comment  (if (coll? i)
                    (list-items i)
                    (str i)))])]))

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
    [:div.row
     [:table#main-table.table.table-hover.table-bordered {:cell-spacing "0" :width "100%"}
      [:thead
       [:tr
        (for [header headers]
          [:th header])]]
      [:tbody
       (for [row data]
         [table-row headers row])]]]))

(defn draw-list []
  [:div
   (comment
    [:table#main-table.table.table-striped.table-bordered {:cell-spacing "0" :width "100%"}
    [:thead
     [:tr [:th "Name"]
      [:th "Age"]]]
    [:tbody
     [:tr [:td "Matthew"]
      [:td "26"]]
     [:tr [:td "Anna"]
      [:td "24"]]
     [:tr [:td "Michelle"]
      [:td "42"]]
     [:tr [:td "Frank"]
      [:td "46"]]]]
    )

   ( draw-table (:data-view @state))

   ])

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
  (.log js/console filter-str)
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
    [:div.col-md-5
     [:input {:type "text"
              :class :form-control
              :value (:filter @state)
              :onChange #(handle-filter (-> % .-target .-value))}]]]
   [:div.row
    [draw-list]]])


(defn home-component []
  (reagent/create-class {:component-function home
                         :component-did-mount home-mounted}))

(reagent/render-component [home] (.getElementById js/document "app"))
(get-data)
