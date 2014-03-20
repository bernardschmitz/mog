
(ns mog.handler
  (:use compojure.core hiccup.core clojure.set ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as json]
            [compojure.route :as route]
            [clojure.java.io :as io]))


(def high-scores (ref [ {:name "Bilbo" :score 20 } { :name "Xtro" :score 10 } ]))

(defn top-score [] ((first @high-scores) :score))

(defn update-high-scores [player]
  (dosync 
    (ref-set high-scores
           (take 20 (sort-by :score > (conj @high-scores player))))))

(def monsters [
               { :name "Adolf Hitler" :hp 1000 }
               { :name "Joseph Stalin" :hp 1000 }
               { :name "Benito Mussolini" :hp 1000 }
               { :name "Pol Pot" :hp 1000 }
               { :name "Idi Amin" :hp 1000 }
               { :name "Saddam Hussein" :hp 1000 }
               { :name "Kim Il-sung" :hp 1000 }
               ])


(def game-id (ref 1000))

(defn format-game-id [id] (format "g%08x" id))

(defn current-game-id [] (format-game-id @game-id))

(defn next-game-id [] (format-game-id (alter game-id inc)))


(def game-states (ref {}))


(defn make-new-game [name] 
  {
    :player {
      :name name :hp 100 :score 0
    }
  })


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
  (map str (take 20 (shuffle letter-pool))))


(defn remove-letter-from-rack [rack letter]
  (let [[h t] (split-with (partial not= letter) rack)]
    (concat h (rest t))))




(defn remove-word-from-rack [rack word]
  (loop [r rack 
         w (map str (seq word))]
         (if (empty? w)
             r
             (recur (remove-letter-from-rack r (first w)) (rest w)))))


(defn letter-in-rack? [rack letter]
  (some #{letter} rack))


(defn word-in-rack? [rack word]
  (loop [r rack
         w (map str (seq word))]
    (let [c (first w)]
    (cond 
      (nil? c) true
      (letter-in-rack? r c) (recur 
                                   (remove-letter-from-rack r c)
                                   (rest w))))))


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




(defn start-game [{{:keys [name] :as params} :params :as req}]
  (prn "req" req)
  (prn "params" params)
  (prn "name" name)
	(prn "gs" @game-states)

  (let [game (make-new-game name)
    	id (dosync (let [id (next-game-id)] (ref-set game-states (assoc @game-states id game)) id))]
	(response (assoc game :gameId id :highScore (top-score) ))))


(defn next-round [{{id :gameId :as params} :params :as req}]
  (prn "req" req)
  (prn "params" params)
  (prn "id" id)

	(prn "prev game" (@game-states id))

	(prn "gs" @game-states)

  (let [game (@game-states id)
	monster (rand-nth monsters)
	letters (random-letters)
	high-score 10000
	initiative "player"
	info '()]
	(dosync 
          (ref-set 
            game-states 
            (assoc 
              @game-states 
              id (assoc 
                   game 
                   :monster monster :letters letters :highScore high-score :initiative initiative :info info))))
	(response (@game-states id))))
	  
(defn add-points [m points]
  (let [score (m :score)]
    (assoc m :score (+ score points))))

(defn apply-damage [m damage]
  (let [hp (m :hp)]
    (assoc m :hp (- hp damage))))


(defn player-play-word [{rack :letters player :player monster :monster info :info :as game} word]
  (let [points (score-word word)
        rack (remove-word-from-rack rack word)]
   (prn word "in rack")

   (prn game)
   (prn rack)

   (assoc game :letters rack :player (add-points player points) :monster (apply-damage monster points)
         :info (conj info (format "%s shouts '%s' and hits %s for %d damage." (player :name) word (monster :name) points)))))


                                                                                                   (defn player-attack [{{id :gameId word :word :as params} :params :as req}]
  (prn "id" id)
  (prn "word" word)

  (prn "game-states" @game-states)
  (prn "game" (@game-states id))

  (let [game (@game-states id)
	rack (game :letters)]
	(prn "game" game)
	(prn "rack" rack)
	(if (valid-word? word)
	    (if (word-in-rack? rack word)
                (let [g (player-play-word game word)]
                  (response 
                    (dosync (ref-set game-states (assoc @game-states id g)) g)))
                (response { :error "word not in letters" }))
            (response { :error "invalid word" }))))

(defn monster-play-word [{rack :letters player :player monster :monster info :info :as game} word]
  (let [points (score-word word)
        rack (remove-word-from-rack rack word)]

   (prn "game" game)
   (prn "rack" rack)
   (prn "points" points)
   
   (assoc game :letters rack :player (apply-damage player points)
          :info (conj info (format "%s screams '%s' and hits %s for %d damage." (monster :name) word (player :name) points)))))



(defn monster-attack [{{id :gameId :as params} :params :as req}]
  (prn "id" id)

  (let [game (@game-states id)
        rack (game :letters)]
    (prn "game" game)
    (prn "rack" rack)
    (let [words (find-words (count rack) dict rack)
          word  (rand-nth (seq words))]
      (prn "words" words)
      (prn "word" word)

      (let [g (monster-play-word game word)]
        (response
         (dosync (ref-set game-states (assoc @game-states id g)) g))))))


(defroutes app-routes
  (GET "/mog/startGame" [] start-game)
  (GET "/mog/nextRound" [] next-round)
  (GET "/mog/playerAttack" [] player-attack)
  (GET "/mog/monsterAttack" [] monster-attack)
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (-> (handler/api app-routes)
;      (json/wrap-json-body)
      (json/wrap-json-response)))

