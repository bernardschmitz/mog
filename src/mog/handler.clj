(ns mog.handler
  (:use compojure.core hiccup.core clojure.set)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(def high-scores (ref [ {:name "bilbo" :score 98765 } { :name "xtro" :score 3737 } ]))
(def top-score ((first @high-scores) :score))

(def letters (seq "abcdefghijklmnopqrstuvwxyz"))
(def vowels [ \a \e \i \o \u ])
(def consonants (difference (set letters) (seq vowels)))

(def letter-scores { \a 1 \b 3 \c 3 \d 2 \e 1 \f 4 \g 2 \h 4 \i 1 \j 8 \k 5 \l 1 \m 3 \n 1 \o 1 \p 3 \q 10 \r 1 \s 1 \t 1 \u 1 \v 4 \w 4 \x 8 \y 4 \z 10 })

(prn letters)
(prn vowels)
(prn consonants)
(prn letter-scores)

(defn login-form []
  (html [:p "Name: "] [:form {:action "/login"}  [:input {:type "text" :name "name"}] [:input {:type "submit"}]]))

(defn login [{{:keys [name]} :params session :session :as req}]
  (prn req)
  {:body (html [:a {:href "/"} "continue"])
   :session (assoc session :game-state {:name name :score 0 :fighting true})}) 

(defn render [{{{:keys [name score] } :game-state} :session :as req}]
  (html [:p "High Score: " top-score] [:br] [:br] [:br] [:p "Score: " score] [:br] [:br]
        [:p "name: " name]))

(defn main-page [{{{:keys [fighting]} :game-state} :session :as req}]
  (prn req)
  (if fighting 
      (render req)
      (login-form)))


(defroutes app-routes
  (GET "/" [] main-page)
  (GET "/login" [] login)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

