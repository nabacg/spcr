(ns spcr.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [POST GET]]
            [clojure.string :refer [blank?]]
            [json-html.core :refer [edn->hiccup]]))

(enable-console-print!)



(defn home []
  [:h1 "Hello World"])

(reagent/render-component [home] (.getElementById js/document "app"))
