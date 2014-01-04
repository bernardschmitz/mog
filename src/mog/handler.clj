(ns mog.handler
  (:use compojure.core hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(def score 12345)
(def high-scores [ {:name "bilbo" :score 98765 } { :name "xtro" :score 3737 } ])
(def top-score ((first high-scores) :score))

(defn main-page [req]
  (html [:p "Score: " score]))

(defroutes app-routes
  (GET "/" [] main-page)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

