(ns snake.app
  (:require [rum.core :as rum]
            [cljs.pprint :as pprint]))

(enable-console-print!)

(def initial-state
  {:paused?      false
   :position     [6 6]
   :history      ()
   :width        12
   :height       12
   :pill-density 0.02
   :pills        #{}                                        ;[4 4] [5 8] ;; generate?
   :velocity     [-1 0]                                     ;; random?
   :size         3
   :dead?        false
   :interval     250})

(defonce !state (atom initial-state))
(def !tick-interval (rum/cursor !state [:interval]))
(def !paused? (rum/cursor !state [:paused?]))
(def !dead? (rum/cursor !state [:dead?]))

(def cell-colors
  {:head "gold"
   :worm "green"
   :pill "red"
   :wall "grey"})

(rum/defc cell-view < rum/static [x y type]
  [:div
   {:style {:float         "left"
            :clear         (if (zero? x) "left")
            :width         "24px"
            :height        "24px"
            :background    (get cell-colors type "#eee")
            :border-radius "0.5em"
            :margin        "1px"
            :border-right  "1px solid #777"
            :border-bottom "1px solid #777"}}])

(defn handle-keys! [!state event]                           ;; ideally this should emit an event, not mutate state directly
  (let [key (.-keyCode event)]
    (case key
      32 (swap! !paused? not)                               ; spacebar
      13 (if @!dead? (reset! !state initial-state))         ; enter
      (when-let [dir (case key
                       37 :left
                       39 :right
                       nil)]
        (.preventDefault event)
        (swap! !state assoc :direction dir))))
  nil)

(defn next-state [{:as state :keys [position size velocity direction pills pill-density width height history]}]
  ;(js/console.log "next state:" state)
  (let [snake         (set (take size history))
        found-pill?   (pills position)
        [dx dy] velocity
        new-vel       (case direction                       ; 90-degree rotation matrices
                        :left [dy (- dx)]
                        :right [(- dy) dx]
                        [dx dy])
        new-pos       (mapv mod (map + new-vel position) [width height])
        pills         (if found-pill? (disj pills position) pills)
        missing-pills (- (* pill-density width height) (count pills))
        new-pills     (take missing-pills (repeatedly #(vector (rand-int width) (rand-int height))))]
    (merge state
           {:dead?     (if (get snake new-pos) true false)
            :direction nil
            :velocity  new-vel
            :history   (take size (conj history position))
            :position  new-pos
            :interval  (- 250 (* 3 size))
            :pills     (into pills new-pills)
            :size      (if found-pill? (inc size) size)})))

(rum/defc world-view < rum/static
  [{:as state :keys [pills size width height history position]}]
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
         (cell-view x y cell-type)))]))

(def last-render (atom nil))

(rum/defc game-container
  < rum/reactive
    {:did-mount    (fn [state]
                     (js/console.log "mount!")
                     (let [key-handler (partial handle-keys! !state)
                           comp        (:rum/react-component state)]
                       (.addEventListener js/window "keydown" key-handler)
                       (assoc state ::interval (js/setInterval #(when (and (not @!dead?) (not @!paused?))
                                                                  (let [last @last-render]
                                                                    (js/console.log "tick!" (- (reset! last-render (js/Date.)) last)))
                                                                  (swap! !state next-state)
                                                                  (rum/request-render comp)) @!tick-interval)
                                    ;(rum/request-render comp)) @!tick-interval)
                                    ::key-handler key-handler)))
     :will-unmount (fn [state]
                     (js/console.log "unmount!" (pr-str (:rum/args state)))
                     (js/clearInterval (::interval state))
                     (.removeEventListener js/window "keydown" (::key-handler state))
                     (dissoc state ::interval ::key-handler))}
  [!state !tick-interval]
  (let [state (rum/react !state)
        interval (rum/react !tick-interval)]
    [:div
     (world-view state)
     [:pre {:style {:clear "left"}}
      "Game State: " (with-out-str (pprint/pprint state))]])) ;(rum/react !state))])

(def size (rum/cursor !state [:size]))

(rum/defc parent-component < rum/reactive [!state]
  (let [state (rum/react !state)]
    [:div
     [:p
      [:button {:on-click #(reset! !state initial-state)} "New Game"]
      [:button {:on-click #(swap! !state next-state)} "Tick!"] " "
      [:button {:on-click #(swap! size inc)} "Grow!"] " "
      [:button {:on-click #(swap! !tick-interval - 15)} "Faster"] " "
      [:button {:on-click #(swap! !tick-interval + 15)} "Slower"] " "
      [:button {:on-click #(swap! !paused? not)} (if @!paused? "Unpause" "Pause")]]
     [:h2 "Score: " (- (rum/react size) (:size initial-state))]
     (if (:dead? state)
       [:div
        [:h2 "Game Over"]]
       ^{:key (:interval state)} (game-container !state !tick-interval))])) ;; !tick-interval]))

(defonce !interval (atom nil))

(defn render! [] ; [canvas]
  (let [el  (.getElementById js/document "canvas")
        ctx (.getContext el "2d")
        w 1000
        h 1000]

    (.clearRect ctx 0 0 w h)
    (set! (.-fillStyle ctx) "#fff")
    (.fillRect ctx 0 0 w h)))

(defn render-cell [ctx x y type]
  (set! (.-fillStyle ctx) (get cell-colors type "#eee"))
  (.fillRect (* 24 x) (* y 24))
  [:div
   {:style {:float         "left"
            :clear         (if (zero? x) "left")
            :width         "24px"
            :height        "24px"
            :background    (get cell-colors type "#eee")
            :border-radius "0.5em"
            :margin        "1px"
            :border-right  "1px solid #777"
            :border-bottom "1px solid #777"}}])

(defn render-loop []
  (js/requestAnimationFrame render-loop)
  (render!))

(defonce !el (atom nil))

(defn ^:export init []
  (js/console.log "init!")
  (rum/mount (parent-component !state) (.getElementById js/document "container")))
