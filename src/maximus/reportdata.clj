(ns maximus.reportdata
  (:require [maximus.core :as m]
            [datomic.api :refer [db] :as d]
            [clojure.pprint :refer [print-table]]
            [maximus.util :as util :refer [->contact ->event map->xml]]
            [clj-time.core :as t]))


(m/init! m/uri)

(def basis (d/basis-t (db m/conn)))

(defn backdate [age]
  (t/minus (t/now) (t/hours age)))


(m/store-contact! m/conn "brian"
                    (->contact "Bob" "Barker" "bb5@pr.com" "Somewhere in Hollywood"
                                 "1234567890" "somesite.com"
                                 (.toDate (backdate 5))))

(m/store-contact! m/conn "brian"
                    (->contact "John" "Doe" "jd@pr.com" "Who knows"
                                 "1234567890" "anothersite.com"
                                 (.toDate (backdate 99))))
(m/store-contact! m/conn "brian"
                    (->contact "Martha" "Washington" "mw@pr.com" "Abington, VA"
                                 "411" "newspaper"
                                 (.toDate (backdate 213))))
(m/store-contact! m/conn "brian"
                    (->contact "Tiger" "Woods" "tw@nike.com" "South FL"
                                 "1234567222" "pgatour.com"
                                 (.toDate (backdate 24))))
(m/store-contact! m/conn "brian"
                    (->contact "Elvis" "Presley" "king@lasvegas.com" "The end of lonely street"
                                 "1238887890" "craigslist.com"
                                 (.toDate (backdate 36))))
(m/store-contact! m/conn "stu"
                    (->contact "Amy" "Pond" "ap@tardis.com" "care of the doctor"
                                 "unlisted" "galafray-times.com"
                                 (.toDate (backdate 84))))

(println
 (m/all-contacts
  (db m/conn)))


(def x (m/all-contacts (db m/conn)))

(defn contacts-response
  []
  (util/element-response (mapv #(map->xml :contact %) x)))
