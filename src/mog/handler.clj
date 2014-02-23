
(ns mog.handler
  (:use compojure.core hiccup.core clojure.set ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.route :as route]
            [clojure.java.io :as io]))


(def high-scores (ref [ {:name "Bilbo" :score 98765 } { :name "Xtro" :score 3737 } ]))
(def top-score ((first @high-scores) :score))

(def monsters [
               { :name "Goon" :hp 1000 }
               ])

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
  (with-open [r (io/reader fname)]
    (doall (line-seq r))))


(def words (load-words "game-words"))


(defn score-word [word]
   (reduce + (map letter-scores word)))


(defn valid-word? [word]
  (some #(= word %) words))


(defn random-letters [] 
  (take 20 (shuffle letter-pool)))


(defn remove-letter-from-rack [rack letter]
  (let [[h t] (split-with (partial not= letter) rack)]
    (concat h (rest t))))


(defn remove-word-from-rack [rack word]
  (if (seq word)
    (remove-word-from-rack (remove-letter-from-rack rack (first word)) (rest word))
    rack))


(defn letter-in-rack? [rack letter]
  (some #{letter} rack))


(defn word-in-rack? [rack word]
  (let [letter (first word)]
    (cond 
      (nil? letter) true
      (letter-in-rack? rack letter) (and true (word-in-rack? (remove-letter-from-rack rack letter) (rest word))))))


(defn add-to-dict [dict word]
  (let [w (apply str (sort word))
        d (get dict w #{})]
    (assoc dict w (conj d word))))


(defn build-dict [words]
  (reduce add-to-dict {} words))


(def dict (build-dict words))


(defn subsets [n items]
  (cond
    (= n 0) '(())
    (empty? items) '()
    :else (concat (map
                    #(cons (first items) %)
                    (subsets (dec n) (rest items)))
                    (subsets n (rest items)))))


(defn find-word [n dict rack]
  (reduce union
    (filter (complement nil?) (map #(dict (apply str (sort %))) (subsets n rack)))))


(defn find-words [n dict rack]
  (reduce union 
    (for [x (range 2 (inc n))] 
      (find-word x dict rack))))


(defn next-monster []
  (rand-nth monsters))


(defn init-game-state [name]
  {:name name :hp 1000 :score 0 :fighting true :rack (random-letters) :foe (next-monster) }) 


(defn login-form []
  (html [:p "Name: "] 
        [:form {:action "/login"}  
          [:input {:type "text" :name "name"}] 
          [:input {:type "submit"}]]))


(defn login [{{:keys [name]} :params session :session :as req}]
  (prn req)
  (->
    (redirect "/")
    (assoc :session (assoc session :game-state (init-game-state name)))))

;  {:body (html [:a {:href "/"} "continue"])
;   :session (assoc session :game-state (init-game-state name))}) 

(defn restart [req]
  ; broken
  (prn "restart")
  (prn "req " req)
  (->
    (redirect "/login")
    (assoc :session nil)))


(defn render-main [{{{:keys [foe rack hp name error score word-score] } :game-state} :session :as req}]
  (html [:p "High Score: " top-score] [:br] [:br] [:p "Score: " score] [:br] [:br]
        [:br]
        [:p "Name: " name]
        [:p "HP: " hp]
        [:br]
        [:p "Foe: " (:name foe)]
        [:p "HP: " (:hp foe)]
        [:br]
        [:p "Letters: " [:font {:size "20px"} (map #(str " " % " ") (sort rack))]]
        (if word-score
          [:p "Hit: " word-score])
        (if error
          [:p "Error: " error])
        [:form {:action "/word"}  
          [:input {:type "text" :name "word"}] 
          [:input {:type "submit"}]]
        ))


(defn play-word [{{:keys [word]} :params {{:keys [rack] :as game-state} :game-state :as session} :session :as req}]
  (prn req)
  (prn word)
  (prn "rack" rack)
  (prn "in rack" (word-in-rack? rack word))
  (prn "valid" (valid-word? word))
  (prn "sess" session)
  (prn "gs" game-state)

  (if (word-in-rack? rack word)
    (if (valid-word? word)
      (let [gs (assoc game-state :error nil :rack (remove-word-from-rack rack word) :word-score (score-word word) )
            r (assoc-in req [ :session :game-state ] gs ) ]
        (->
          (response (render-main r))
          (assoc :session { :game-state gs })))

      (let [gs (assoc game-state :word-score nil :error "Invalid word" )
            r (assoc-in req [ :session :game-state ] gs ) ]
        (->
          (response (render-main r))
          (assoc :session { :game-state gs }))))

    (let [gs (assoc game-state :word-score nil :error "Wrong letters" )
          r (assoc-in req [ :session :game-state ] gs ) ]
      (->
        (response (render-main r))
        (assoc :session { :game-state gs })))))


; user=> (let [ { { { :keys [c] :as b } :b :as a } :a :as req }  { :a { :b { :c 1 } } } ] (prn a b c req))
; {:b {:c 1}} {:c 1} 1 {:a {:b {:c 1}}}


(defn main-page [{{{:keys [fighting] :as game-state } :game-state :as session } :session :as req}]
  (prn "req" req)
  (prn "game" game-state ) 
  (prn "sess" session ) 
  (if fighting 
      (-> (response (render-main req)) 
        (assoc :session { :game-state game-state }))
      (login-form)))


(defroutes app-routes
;  (GET "/" [] main-page)
  (GET "/login" [] login)
  (GET "/word" [] play-word)
  (GET "/restart" [] restart)
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))

