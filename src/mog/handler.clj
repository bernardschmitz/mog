(ns mog.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/fancy/:name" [name]
       (str "Hello, " name))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

