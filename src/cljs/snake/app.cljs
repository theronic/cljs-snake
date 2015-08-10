(ns snake.app
  (:require [reagent.core :as reagent :refer [atom cursor]]))

(defn parent-component []
  [:div
   {:style {:text-align "center"
            :margin-top "3em"}}
   [:h1 "The State of The Art in Front End Development"]
   [:h2 "A Foray Into ClojureScript"]
   [:h3 "by "
    [:a {:href "http://petrustheron.com"}
     "Petrus Theron"] " on 12 August 2015"]])

(defn ^:export mount-component []
  (reagent/render-component [parent-component]
    (.getElementById js/document "container")))

(defn ^:export init []
  (mount-component))