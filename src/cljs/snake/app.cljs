(ns snake.app
  (:require [reagent.core :as r :refer [atom cursor]]))

(enable-console-print!)

(def width 12)
(def height 12)
(def !tick-interval (atom 250))
(def min-pills (int (* width height 0.02)))
(def init-size 3)

(def initial-state
  {:paused?  false
   :position [(int (/ width 2)) (int (/ height 2))]
   :history  ()
   :velocity [-1 0]
   :size     init-size
   :dead?    false
   :pills    #{[4 4] [5 8]}})

(defonce !state (atom initial-state))
(def !paused? (cursor !state [:paused?]))
(def !dead? (cursor !state [:dead?]))

(def cell-colors
  {:head "gold"
   :worm "green"
   :pill "red"
   :wall "grey"})

(defn cell-view [x y type]
  [:div
   {:style {:float         "left"
            :clear         (if (zero? x) "left")
            :width         "24px"
            :height        "24px"
            :background    (get cell-colors type "#eee")
            :border-radius "0.5em"
            :margin "1px"
            :border-right  "1px solid #777"
            :border-bottom "1px solid #777"}}])

(defn handle-keys! [event]
  (let [key (.-keyCode event)]
    (case key
      32 (swap! !paused? not)                            ; spacebar
      13 (if @!dead? (reset! !state initial-state))          ; enter
      (when-let [dir (case key
                           37 :left
                           39 :right
                           nil)]
        (.preventDefault event)
        (swap! !state assoc :direction dir))))
  nil)

(defn next-state [{:keys [position size velocity direction pills history] :as state}]
  (let [snake         (set (take size history))
        found-pill?   (pills position)       
        [dx dy]       velocity
        new-vel       (case direction                       ; 90-degree rotation matrices
                            :left [dy (- dx)]
                            :right [(- dy) dx]
                            [dx dy])
        new-pos       (mapv mod (map + new-vel position) [width height])
        pills         (if found-pill? (disj pills position) pills)
        missing-pills (- min-pills (count pills))
        new-pills     (take missing-pills (repeatedly #(vector (rand-int width) (rand-int height))))]
    (assoc state
      :dead? (if (snake new-pos) true false)
      :direction nil
      :velocity new-vel
      :history (take 100 (conj history position))
      :position new-pos
      :pills (into pills new-pills)
      :size (if found-pill? (inc size) size))))

(defn world-view [{:as state :keys [pills size history position]}]
  (let [worm (set (take size history))]
    [:div
     (for [y (range height)
           x (range width)]
       (let [pos       [x y]
             cell-type (cond
                         (= position pos) :head
                         (worm pos) :worm
                         (pills pos) :pill)]
         ^{:key [x y]}
         [cell-view x y cell-type]))]))

(defn game-container [!state]
  (r/with-let [_ (js/window.addEventListener "keydown" handle-keys!)
               tick! #(if (and (not @!dead?) (not @!paused?))
                        (swap! !state next-state))
               interval (js/setInterval tick! @!tick-interval)]
              [world-view @!state]
              (finally
                (js/clearInterval interval)
                (js/window.removeEventListener "keydown" handle-keys!))))

(defn parent-component []
  (let [size (cursor !state [:size])]
    [:div
     [:p
      [:button {:on-click #(reset! !state initial-state)} "New Game"] " "
      [:button {:on-click #(swap! !state next-state)} "Tick!"] " "
      [:button {:on-click #(swap! size inc)} "Grow!"] " "
      ;[:button {:on-click #(swap! !tick-interval - 10)} "Faster"] " "
      ;[:button {:on-click #(swap! !tick-interval + 10)} "Slower"] " "
      [:button {:on-click #(swap! !paused? not)} (if @!paused? "Unpause" "Pause")]]
     [:h2 "Score: " (- (:size @!state) init-size)]
     (if (:dead? @!state)
       [:div
        [:h2 "Game Over"]
        [:h3 "Press enter to try again :)"]]
       [game-container !state])
     [:pre {:style {:clear "left" :white-space "normal"}}
      "Game State: " (pr-str @!state)]]))

(defn ^:export init []
  (r/render-component [parent-component] (.getElementById js/document "container")))