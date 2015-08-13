(ns snake.app
  (:require [reagent.core :as reagent :refer [atom cursor]]))

(enable-console-print!)

(def width 10)
(def height 10)
(def tick-interval 200)
(def min-pills 3)
(def init-size 3)

(def init-state
  {:paused?  false
   :position [7 7]
   :history  ()
   :velocity [-1 0]
   :size     init-size
   :dead?    false
   :pills    #{[4 4] [5 8]}})

(defonce state (atom init-state))
(def paused? (cursor state [:paused?]))
(def dead? (cursor state [:dead?]))

(def cell-colors
  {:head "gold"
   :worm "green"
   :pill "red"
   :wall "grey"})

(defn multiply-matrix
  "Multiplies a vector by the columns of a matrix. For rotation."
  [v cols]
  (mapv #(apply + (map * % v)) cols))

(defn cell-view [x y type]
  [:div
   {:style {:float         "left"
            :clear         (if (zero? x) "both")
            :width         "20px"
            :height        "20px"
            :background    (get cell-colors type "#eee")
            :border-radius "0.5em"
            :margin "1px"
            :border-right  "1px solid #777"
            :border-bottom "1px solid #777"}}])

(def rotations
  "Rotation matrices."
  {:left [[0 1] [-1 0]]
   :right [[0 -1] [1 0]]})

(defn handle-keys! [event]
  (let [key (.-keyCode event)]
    (if (= key 32)                                          ; 32 is spacebar
      (swap! paused? not)
      (when-let [dir (case key
                           37 :left
                           39 :right
                           nil)]
        (.preventDefault event)
        (swap! state assoc :direction dir))))
  nil)

(defn next-state [{:keys [position size velocity direction pills history] :as state}]
  (let [snake         (set (take size history))
        found-pill?   (pills position)
        rotation      (get rotations direction [[1 0] [0 1]])
        velocity      (multiply-matrix velocity rotation)
        new-pos       (mapv mod (map + velocity position) [width height])
        pills         (if found-pill? (disj pills position) pills)
        missing-pills (- min-pills (count pills))
        new-pills     (take missing-pills (repeatedly #(vector (rand-int width) (rand-int height))))]
    (assoc state
      :dead? (if (snake new-pos) true false)
      :direction nil
      :velocity velocity
      :history (take 30 (conj history position))
      :position new-pos
      :pills (into pills new-pills)
      :size (if found-pill? (inc size) size))))

(defn tick! []
  (if (and (not @dead?) (not @paused?))
    (swap! state next-state)))

(defn world-view [{:keys [pills size history position] :as state}]
  (let [worm (set (take size history))]
    [:div
     (for [y (range height)
           x (range width)]
       (let [pos       [x y]
             cell-type (cond
                         (= position pos) :head
                         (worm pos)       :worm
                         (pills pos)      :pill)]
         ^{:key [x y]}
         [cell-view x y cell-type]))]))

(defn parent-component []
  (let [size (cursor state [:size])]
    [:div
     [:h1 "Let's make snake!"]
     [:button {:on-click #(reset! state init-state)} "New Game"]
     [:button {:on-click #(tick!)} "Tick!"]
     [:button {:on-click #(swap! size inc)} "Grow!"]
     [:button {:on-click #(swap! paused? not)} "Pause"]
     (if (:dead? @state)
       [:h2 "Game Over. Score: " (- (:size @state) init-size)]
       [world-view @state])
     [:pre {:style {:clear "left" :font-size "1.6em" :white-space "normal"}}
      (str (dissoc @state :history))]]))

(defn ^:export mount-component []
  (reagent/render-component [parent-component]
    (.getElementById js/document "container")))

(defn ^:export init []  
  (.addEventListener js/window "keydown" #(handle-keys! %))
  (js/setInterval #(tick!) tick-interval)
  (mount-component))