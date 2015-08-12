(ns snake.app
  (:require [reagent.core :as reagent :refer [atom cursor]]))

(enable-console-print!)

(def width 15)
(def height 15)
(def min-pills 3)
(def paused? (atom false))

(def init-state {:position [7 7]
                 :history ()
                 :velocity [-1 0]
                 :size 3
                 :pills #{[4 4] [5 8]}})

(defonce state (atom init-state))

(def colors
  {:head "gold"
   :worm "green"
   :pill "red"
   :wall "grey"})

(defn cell-view [x y cell]
  [:div
   {:style {:float         "left"
            :clear         (if (zero? x) "both")
            :width         "32px"
            :height        "32px"
            :background    (get colors cell "#eee")
            :border-right  "1px solid #222"
            :border-bottom "1px solid #222"}}])

(defn world-view [{:keys [pills size history position] :as state}]
  (let [worm (set (take size history))]
    [:div
     (for [y (range height)
           x (range width)]
       (let [pos  [x y]
             cell (cond
                    (= position [x y]) :head
                    (pills pos) :pill
                    (worm pos) :worm)]
         ^{:key [x y]}
         [cell-view x y cell]))]))

(def arrow-keys
  {37 :left
   38 :up
   39 :right
   40 :down
   32 :space})

(def directions
  {:left [-1 0]
   :up [0 -1]
   :right [1 0]
   :down [0 1]})

(defn handle-keys! [e]
  (let [key (get arrow-keys (.-keyCode e))]
    (if (= key :space)
      (swap! paused? not)
      (if-let [new-vel (get directions key)]
        (do
          (swap! state assoc :velocity new-vel)
          false)
        true))))

(defn setup-keys! []
  (.addEventListener js/window "keydown" #(handle-keys! %)))

(defn next-state [{:keys [pills size history position velocity] :as state}]
  (let [pill? (pills position)
        new-pos (mapv mod (map + velocity position) [width height])
        pills (if pill? (disj pills position) pills)
        new-pills (reduce (fn [acc v] (conj acc v)) pills (take (- min-pills (count pills)) (repeatedly (fn [] [(rand-int width) (rand-int height)]))))]
    (assoc state
      :history (take 30 (conj history position))
      :position new-pos
      :pills new-pills
      :size (if pill? (inc size) size))))

(defn tick! []
  (if-not @paused?
    (swap! state next-state)))

(defn setup-interval! []
  (js/setInterval #(tick!) 200))

(defn parent-component []
  (let [size (cursor state [:size])]
    [:div
     [:h1 "Let's make snake!"]
     [:button {:on-click #(reset! state init-state)} "Reset!"]
     [:button {:on-click #(tick!)} "Tick!"]
     [:button {:on-click #(swap! size inc)} "Grow!"]
     [:button {:on-click #(swap! paused? not)} "Pause"]
     [:pre {:style {:font-size "1.6em" :white-space "normal"}}
      (str (dissoc @state :history))]
     [world-view @state]]))

(defn ^:export mount-component []
  (reagent/render-component [parent-component]
    (.getElementById js/document "container")))

(defn ^:export init []
  (setup-interval!)
  (setup-keys!)
  (mount-component))