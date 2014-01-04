(ns mog.handler
  (:use compojure.core hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(ref high-scores [ {:name "bilbo" :score 98765 } { :name "xtro" :score 3737 } ])

(def score 12345)
(def top-score ((first @high-scores) :score))

(defn main-page [req]
  (html [:p "High Score: " top-score] [:br] [:br] [:br] [:p "Score: " score] [:br] [:br]
        [:p "name: " ((req :session) :name)]))

(defroutes app-routes
  (GET "/" [] main-page)
  (GET "/login/:name" [name] 
      {:redirect "/" :session {:name name :score 0}}) 
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

