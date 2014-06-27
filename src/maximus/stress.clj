(ns maximus.stress
  (:require [maximus.core :as m]
            [maximus.pgdata :as p]
            [datomic.api :refer [db] :as d]
            [maximus.util :as util :refer [->contact ->event]]
            [clj-time.core :as t]))

;; Database functions
(def +database+ "datomic")

(if (= +database+ "postgres")
  (do
    (def +store-card-fn+ (partial p/store-contact! p/dbinfo))
    (def +active-count-fn+ #(p/active-count p/dbinfo))
    (def +active-cards-fn+ #(p/active-cards p/dbinfo))
    (def +store-event-fn+ (partial p/store-event! p/dbinfo))
    (def +update-card-fn+ (fn [contact agent new-status]
                            (p/update-contact! contact p/dbinfo agent
                                                 :contact/status new-status)))
    (def +update-lead-fn+ (fn [contact user value]
                                  (p/update-contact! contact p/dbinfo user
                                                       :leadsource value))))
  (do
    (m/init! m/uri)
    (def +store-card-fn+ (partial m/store-contact! m/conn))
    (def +active-count-fn+ #(count (m/active-cards (db m/conn))))
    (def +active-cards-fn+ #(m/active-cards (db m/conn)))
    (def +store-event-fn+ (partial m/store-event! m/conn))
    (def +update-card-fn+ (fn [contact agent new-status]
                            (m/assert! contact m/conn agent
                                       :contact/status
                                       (keyword "contact.status" new-status))))
    (def +update-lead-fn+ (fn [contact user value]
                                  (m/assert! contact m/conn user
                                             :contact/lead-source value)))))

;; Generator functions
(def names ["adam" "bill" "cliff" "doug" "edward" "frank"])
(def more-names (concat names ["amy" "brenda" "christie" "dana" "erin"
                               "fiona" "gina" "harry" "ivan" "joe"
                               "kelly" "lee" "mary" "nan" "otto" "pat"
                               "quin" "randy" "stu" "tom" "utah" "vic"
                               "wyatt" "xander" "yolanda" "zabb"]))
(def lead-sources ["alpha" "beta" "gamma" "delta"])
(def alphabet (map char (range 97 123)))
(defn random-agent [] (rand-nth names))
(def event-types [:event.type/visit :event.type/phone-call
                  :event.type/email :event.type/misc :event/type/text
                  :event.type/sold :event.type/lost])
(defn random-event [] (rand-nth event-types))
(defn random-string []
  (let [len (+ 5 (rand 10))]
    (apply str (take len (repeatedly #(rand-nth alphabet))))))
(defn random-email []
  (str (random-string) "@" (random-string) ".com"))
(defn random-postal []
  (apply str
         (interpose " " [(+ 1 (rand-int 9999))
                     (random-string)
                     (when (rand-int 2) (random-string))
                     "street"])))
(defn random-phone []
  (apply str (take 10 (repeatedly #(rand-nth (range 10))))))
(defn random-user [] (rand-nth more-names))
(defn random-lead-source []
  (rand-nth lead-sources))
(def start-date (-> 5 t/years t/ago))
(defn random-interval []
  (t/seconds (+ 1 (rand-int 1000))))
(def time-gen (iterate #(t/plus % (random-interval)) start-date))
(defn random-card [created-on]
  (->contact (random-string) (random-string) (random-email) (random-postal)
               (random-phone) (random-lead-source) created-on))


;; simulation

(defn add-card [created-on]
  (+store-card-fn+ (random-user) (random-card created-on)))

(defn new-status [contact agent new-status]
  (+update-card-fn+ contact agent new-status))

(defn add-event [contact]
  (let [event-type (random-event)
        agent (random-agent)]
    (+store-event-fn+ (random-user) contact
                      (->event event-type agent))
    (when (or (= event-type :event.type/sold)
              (= event-type :event.type/lost))
      (new-status contact agent (name event-type)))))

(defn update-card [contact]
  (let [user (random-user)]
    (+update-lead-fn+ contact (random-user) (random-lead-source))))

(defn update-cards []
  (doseq [card (+active-cards-fn+)]
    (update-card card)))

(defn add-events []
  (doseq [card (+active-cards-fn+)]
    (add-event card)))

(defn card-gen [n]
  (loop [i n times time-gen]
    (when-not (zero? i)
      (add-card (.toDate (first times)))
      (recur (dec i) (next times)))))

(defn workout []
  (time
   (dotimes [n 5]
     (card-gen 10000)
     (dotimes [n 1]
       (update-cards))
     (add-events)
     (println (+active-count-fn+)))))

(comment
  (workout)
)
