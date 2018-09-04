(set-env!
 :source-paths    #{"src/cljs"}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "2.1.4" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3"      :scope "test"]
                 [adzerk/boot-reload    "0.6.0"      :scope "test"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [weasel "0.7.0" :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [pandeiro/boot-http    "0.8.3"      :scope "test"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[clojure.java.io :as io]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]])

(deftask build []
  (comp (speak)
        (cljs)))


(deftask run []
  (comp (serve :port 8002)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :source-map true}
                 reload {:on-jsload 'snake.app/init})
  identity)

(deftask generate-index-page [] ; null pointer why?
         (->> (slurp "src/cljs/snake/app.cljs")
             (spit "resources/src/app.cljs")))

(deftask prod []
  (comp (production)
        (watch)
        ;(generate-index-page)
        (cljs)
        (target :dir #{"target/prod"})))

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        ;(generate-index-page)
        (run)))


