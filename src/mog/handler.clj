
(ns mog.handler
  (:use compojure.core hiccup.core ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as json]
            [compojure.route :as route]
            [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))


(def high-scores (ref [ {:name "Bilbo" :score 20 } { :name "Xtro" :score 10 } ]))

(defn top-score [] ((first @high-scores) :score))

(defn update-high-scores [player]
  (dosync 
    (ref-set high-scores
           (take 20 (sort-by :score > (conj @high-scores player))))))

(def monsters [
                { :name "Satan Cocker" :title "Legendary Rooster From Hell" :hp 1000 :attack "clucks" }
                { :name "Ouroboros" :title "The Infinite Coil" :hp 1000 :attack "hisses" }
                { :name "Asterion" :title "Cursed Scion Of Minos" :hp 1000 :attack "moos" }
                { :name "Callisto" :title "Queen Of The Woods" :hp 1000 :attack "growls" }
                { :name "Frog" :title "Frenzied Servant Of Magog" :hp 1000 :attack "croaks" }
                { :name "Serpentarius" :title "The Forgotten Zodiac" :hp 1000 :attack "roars" }
                { :name "Wendigo" :title "Endless Storm" :hp 1000 :attack "screams" }
                { :name "Behemoth" :title "The Walking Earthquake" :hp 1000 :attack "trumpets" }
                { :name "Leviathan" :title "The Evil Beneath The Waves" :hp 1000 :attack "bubbles" }
                { :name "Eohippus" :title "The Abandoned Prototype" :hp 1000 :attack "neighs" }
                { :name "Kron-Pirr" :title "The Last Of The Anunnaki" :hp 1000 :attack "whispers" }
                { :name "Kord" :title "The Living Extension" :hp 1000 :attack "utters" }
                { :name "Titanosaur" :title "The Ancient Lizard King" :hp 1000 :attack "roars" }
               ])



(def game-id (ref 1000))

(defn format-game-id [id] (format "g%08x" id))

(defn current-game-id [] (format-game-id @game-id))

(defn next-game-id [] (format-game-id (alter game-id inc)))


(def game-states (ref {}))


(defn make-new-game [] 
  {
    :player {
      :name "Noah" 
      :title "Immortal Zookeeper"
      :hp 1000 :score 0
    }
  })


(def letters (seq "abcdefghijklmnopqrstuvwxyz"))
(def vowels [ \a \e \i \o \u ])
(def consonants (difference (set letters) (seq vowels)))

;(def letter-scores { \a 1 \b 3 \c 3 \d 2 \e 1 \f 4 \g 2 \h 4 \i 1 \j 8 \k 5 \l 1 \m 3 \n 1 \o 1 \p 3 \q 10 \r 1 \s 1 \t 1 \u 1 \v 4 \w 4 \x 8 \y 4 \z 10 })
(def letter-scores { \a 1 \e 1 \h 1 \i 1 \n 1 \o 1 \r 1 \s 1 \t 1
                     \b 2 \c 2 \d 2 \f 2 \g 2 \l 2 \m 2 \p 2 \u 2 \w 2 \y 2
                     \v 3 \j 3 \k 3 \q 3 \x 3 \z 3 })


(def score-letters {
                    1 (for [[k v] letter-scores :when (= 1 v)] k)
                    2 (for [[k v] letter-scores :when (= 2 v)] k)
                    3 (for [[k v] letter-scores :when (= 3 v)] k)
                    })




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
  (* (count word) (reduce * (map letter-scores word))))

(defn square [x]
  (* x x))

(defn score-free-attack [word] 
  (square (reduce * (map letter-scores word))))


(defn valid-word? [word]
  (some #(= word %) words))


(defn random-letter []
  (let [k (rand)]
    (cond 
     (< k 0.1270) \e
     (< k 0.2176) \t
     (< k 0.2993) \a
     (< k 0.3743) \o
     (< k 0.4440) \i
     (< k 0.5115) \n
     (< k 0.5747) \s
     (< k 0.6357) \h
     (< k 0.6956) \r
     (< k 0.7381) \d
     (< k 0.7783) \l
     (< k 0.8062) \c
     (< k 0.8337) \u
     (< k 0.8578) \m
     (< k 0.8814) \w
     (< k 0.9037) \f
     (< k 0.9238) \g
     (< k 0.9436) \y
     (< k 0.9629) \p
     (< k 0.9778) \b
     (< k 0.9876) \v
     (< k 0.9953) \k
     (< k 0.9968) \j
     (< k 0.9983) \x
     (< k 0.9993) \q
     (< k 0.1000) \z)
    ))


;(defn random-letter [x y z]
;  (let [q (- 20 x y z)]
;    (concat 
;     (for [_ (range x) ] (rand-nth (score-letters 1)))
;     (for [_ (range y) ] (rand-nth (score-letters 2)))
;     (for [_ (range z) ] (rand-nth (score-letters 3)))
;     (for [_ (range q) ] (rand-nth (keys letter-scores))))))


(defn random-letters [] 
;  (map str (random-letter 7 5 3)))
  (map str (for [_ (range 0 20)] (random-letter))))


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
  (reduce set/union
    (filter (complement nil?) (map #(dict (apply str (sort %))) (subsets n rack)))))


(defn find-words [n dict rack]
  (reduce set/union 
    (for [x (range 2 (inc n))] 
      (find-word x dict rack))))

(defn all-words [dict rack]
   (seq (find-words (count rack) dict rack)))

(defn random-word [dict rack]
   (rand-nth (all-words dict rack)))

(defn longest-word [dict rack]
  (first (reverse (sort-by count (all-words dict rack)))))

(defn best-word [dict rack]
  (first (reverse (sort-by score-word (all-words dict rack)))))

(defn worst-word [dict rack]
  (first (sort-by count (all-words dict rack))))

(defn shortest-word [dict rack]
  (first (sort-by score-word (all-words dict rack))))


(defn monster-word-gen [name]
  best-word)


(defn start-game [{{:keys [name] :as params} :params :as req}]
  (prn "req" req)
  (prn "params" params)
  (prn "name" name)
	(prn "gs" @game-states)

  (let [game (make-new-game)
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

(defn add-info [{info :info :as game} message]
  (assoc game :info (conj info message)))

(defn update-letters [game letters]
  (assoc game :letters letters))

(defn update-player [game player]
  (assoc game :player player))

(defn update-monster [game monster]
  (assoc game :monster monster))

(defn no-more-words? [dict rack]
  (prn "rack " rack)
  (let [words (all-words dict rack)]
    (prn "remaining words " words)
    (empty? words)))

(defn free-attack [{rack :letters :as game} us-key them-key]
    (let [them (game them-key)
          us (game us-key)
          word (string/join rack)
          points (score-free-attack word)
          rack (remove-word-from-rack rack word)]
      (prn us)
      (prn them)
      (prn word)
      (prn points)
      (prn rack)
      (-> (assoc game them-key (apply-damage them points))
        (update-letters rack)
        (add-info (format "%s hits %s with remaining letters '%s' for %d damage." (us :name) (them :name) word points)))))



(defn check-round-end [game us them]
  (let [rack (game :letters)]
      (if (no-more-words? dict rack)
        (-> (assoc game :round-end true)
            (free-attack us them))
        (assoc game :round-end false))))


(defn player-play-word [{rack :letters player :player monster :monster :as game} word]
  (let [points (score-word word)
        rack (remove-word-from-rack rack word)]

   (prn word "in rack")
   (prn game)
   (prn rack)

    (-> (update-letters game rack)
        (update-player (add-points player points))
        (update-monster (apply-damage monster points))
        (add-info (format "%s speaks '%s' and hits %s for %d damage." (player :name) word (monster :name) points)))))


;   (assoc game :letters rack :player (add-points player points) :monster (apply-damage monster points)
;         :info (conj info (format "%s speaks '%s' and hits %s for %d damage." (player :name) word (monster :name) points)))))


(defn update-game [id game]
  (dosync (ref-set game-states (assoc @game-states id game)) game))


(defn player-attack [{{id :gameId word :word :as params} :params :as req}]
  (prn "id" id)
  (prn "word" word)

  (prn "game-states" @game-states)
  (prn "game" (@game-states id))

  (let [game (@game-states id)
	rack (game :letters)
        word (string/lower-case word)]
	(prn "game" game)
	(prn "rack" rack)
	(if (valid-word? word)
	    (if (word-in-rack? rack word)
                (let [g (-> (player-play-word game word) (check-round-end :player :monster))]
                  (response (update-game id g)))
                (response { :error "word not in letters" }))
            (response { :error "invalid word" }))))



(defn monster-play-word [{rack :letters player :player monster :monster info :info :as game} word]
  (let [points (score-word word)
        rack (remove-word-from-rack rack word)]

   (prn "game" game)
   (prn "rack" rack)
   (prn "points" points)

    (-> (update-letters game rack)
        (update-player (apply-damage player points))
        (add-info (format "%s %s '%s' and hits %s for %d damage." (monster :name) (monster :attack) word (player :name) points)))))



(defn monster-attack [{{id :gameId :as params} :params :as req}]
  (prn "id" id)

  (let [game (@game-states id)
        rack (game :letters)]
    (prn "game" game)
    (prn "rack" rack)
    ;(let [word  (rand-nth (seq (find-words (count rack) dict rack)))]
    (let [word-gen (monster-word-gen ((game :monster) :name))
          word   (word-gen dict rack)]
      (prn "word" word)

      (let [g (-> (monster-play-word game word) (check-round-end :monster :player))]
        (response (update-game id g))))))


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

