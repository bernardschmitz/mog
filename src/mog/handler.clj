(ns mog.handler
  (:use compojure.core hiccup.core clojure.set clojure.java.io :as io)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(def high-scores (ref [ {:name "bilbo" :score 98765 } { :name "xtro" :score 3737 } ]))
(def top-score ((first @high-scores) :score))

(def letters (seq "abcdefghijklmnopqrstuvwxyz"))
(def vowels [ \a \e \i \o \u ])
(def consonants (difference (set letters) (seq vowels)))

(def letter-scores { \a 1 \b 3 \c 3 \d 2 \e 1 \f 4 \g 2 \h 4 \i 1 \j 8 \k 5 \l 1 \m 3 \n 1 \o 1 \p 3 \q 10 \r 1 \s 1 \t 1 \u 1 \v 4 \w 4 \x 8 \y 4 \z 10 })

(def letter-pool (concat
  (repeat 9 \a) (repeat 2 \b) (repeat 2 \c) (repeat 4 \d) (repeat 12 \e) (repeat 2 \f) (repeat 3 \g)
  (repeat 2 \h) (repeat 9 \i) (repeat 1 \j) (repeat 1 \k) (repeat 4 \l) (repeat 2 \m) (repeat 6 \n)
  (repeat 8 \o) (repeat 2 \p) (repeat 1 \q) (repeat 6 \r) (repeat 4 \s) (repeat 6 \t) (repeat 4 \u)
  (repeat 2 \v) (repeat 2 \w) (repeat 1 \x) (repeat 2 \y) (repeat 1 \z)))

(prn letters)
(prn vowels)
(prn consonants)
(prn letter-scores)
(prn letter-pool)

 
(defn load-words [fname]
  (with-open [r (reader fname)]
    (doall (line-seq r))))

(def words (load-words "game-words"))

(defn score-word [word]
   (reduce + (map letter-scores word)))

(defn random-letters [] 
  (take 20 (shuffle letter-pool)))

(defn init-game-state [name]
  {:name name :hp 1000 :score 0 :fighting true :letters (random-letters)}) 

(defn login-form []
  (html [:p "Name: "] [:form {:action "/login"}  [:input {:type "text" :name "name"}] [:input {:type "submit"}]]))

(defn login [{{:keys [name]} :params session :session :as req}]
  (prn req)
  {:body (html [:a {:href "/"} "continue"])
   :session (assoc session :game-state (init-game-state name))}) 

(defn render [{{{:keys [letters hp name score] } :game-state} :session :as req}]
  (html [:p "High Score: " top-score] [:br] [:br] [:br] [:p "Score: " score] [:br] [:br]
        [:p "Name: " name]
        [:p "HP: " hp]
        [:p "Letters: " (map str letters)]
        ))

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

