(ns spcr.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [POST GET]]
            [clojure.string :refer [blank?]]
            [json-html.core :refer [edn->hiccup]]))

(enable-console-print!)



(def state (atom {:raw-data []
                  :filter nil
                  :data-view []}))

(defn get-data []
  (GET "/data"
       {
        :format :json
        :handler (fn [raw-data] (swap! state #(-> %
                                                 (assoc :raw-data raw-data)
                                                 (assoc :data-view raw-data))))}))

(defn list-items [lst]
  [:ul.list-group
   (for [i lst]
     [:li.list-group-item
      (str i)
      (comment  (if (coll? i)
                  (list-items i)
                  (str i)))])])

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
     [:table.table.table-hover.table-bordered
      [:tr
       (for [header headers]
         [:th header])]
      (for [row data]
        [table-row headers row])]]))

(defn draw-list []
  [:div
   ( draw-table (:data-view @state))])

(defn handle-filter [filter-str]
  (.log js/console filter-str)
  (let [filtered-data (if (not (blank? filter-str))
                        (filter (fn [row]
                                  (some (fn [val]
                                          (re-find (re-pattern filter-str) (str val)))
                                        (vals row)))
                                (:raw-data @state))
                        (:raw-data @state))]
    (swap! state #(-> %
                      (assoc :filter filter-str)
                      (assoc :data-view filtered-data)))))

(defn home []
  [:div
   [:div.row
    [:div.col-md-2]
    [:div.col-md-10
     [:h3 "TEST"]]]
   [:div.row
    [:div.col-md-5
     [:input {:type "text"
              :class :form-control
              :value (:filter @state)
              :onChange #(handle-filter (-> % .-target .-value))}]]]
   [:div.row
    [draw-list]]])




(reagent/render-component [home] (.getElementById js/document "app"))
(get-data)
