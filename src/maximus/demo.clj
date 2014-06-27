(ns maximus.demo
  (:require [maximus.core :as m]
            [datomic.api :refer [db] :as d]
            [clojure.pprint :refer [print-table]]
            [maximus.util :refer [->contact ->event]]))

(m/init! m/uri)
(def basis (d/basis-t (db m/conn)))

(defn print-audit!
  []
  (print-table (m/audit-log m/conn basis)))

(defn print-contacts!
  []
  (let [db (db m/conn)]
    (print-table
     (remove #{:contact/event} (m/contact-attributes db))
     (m/all-contacts db))))


(print-audit!)

(m/store-contact! m/conn "brian"
 (->contact "Bob" "Barker" "bb@pr.com" "Somewhere in Hollywood"
              "1234567890" "somesite.com" nil))

(print-audit!)
(print-contacts!)

(m/store-contact! m/conn "tom"
 (->contact "Tiny" "Tim" "tt@pr.com" "Somewhere else in Hollywood"
              "0987654321" "anothersite.com" nil))

(m/store-contact! m/conn "tom"
 (->contact "Peter" "Paul" "pp@pr.com" "Somewhere not in Hollywood"
              "5551212" "craigslist.com" nil))

(print-audit!)
(print-contacts!)

(m/store-event! m/conn "Mary"
                (m/contact (db m/conn) "bb@pr.com")
                (->event :event.type/showing "Smith"))
(m/store-event! m/conn "Mary"
                (m/contact (db m/conn) "bb@pr.com")
                (->event :event.type/phone-call "Smith"))

(print-audit!)

(m/assert! (m/contact (db m/conn) "tt@pr.com") m/conn "Eve"
           :contact/lead-source "company website")

(print-audit!)
(print-contacts!)

(m/add-contact-attrib! m/conn "brian"
   "special-needs?" "boolean" "single")

(print-contacts!)
(print-audit!)

(m/assert! (m/contact (db m/conn) "tt@pr.com") m/conn "brian"
           :contact/special-needs? true)

(print-contacts!)
