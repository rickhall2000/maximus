(ns maximus.core-test
  (:require [clojure.test :refer :all]
            [maximus.core :refer :all]
            [maximus.util :refer :all]
            [datomic.api :as d]))

(defn start-db []
  (let [uri "datomic:mem://max-test-db"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn (read-file +schema-file+))
      conn)))

(def demo-user
  {:db/id #db/id [:db.part/user -1]
   :contact/first-name "jonathan"
   :contact/last-name "hart"
   :contact/email-address "jhart@gmail.com"
   :contact/postal-address "123 Fake Street"
   :contact/status :contact.status/active})

(def demo-user2
  {:db/id #db/id [:db.part/user -1]
   :contact/first-name "elvis"
   :contact/last-name "presley"
   :contact/email-address "theking@graceland.com"
   :contact/postal-address "lonely "
   :contact/status :contact.status/active})


(deftest store-contact-test
  (let [conn (start-db)
        old-db (d/db conn)
        tran (store-contact! conn "test user" demo-user)
        new-db (d/db conn)]
    (is (zero? (count (all-contacts old-db ))))
    (is (= "hart" (:contact/last-name
                   (contact new-db
                              (:contact/email-address demo-user)))))
    (is (= 1 (count (all-contacts new-db))))))

(deftest event-test
  (let [conn (start-db)]
    (do
      (store-contact! conn "test user" demo-user)
      (store-contact! conn "test user" demo-user2)
      (is (= 2 (count (all-contacts (d/db conn)))))
      (store-event! conn "second user"
         (contact (d/db conn)
                    (:contact/email-address demo-user))
         (->event :event-type/phone-call "Smith"))
      (store-event! conn "second user"
         (contact (d/db conn)
                    (:contact/email-address demo-user))
         (->event :event-type/showing "Smith"))
      (is (= 2
             (count
              (:contact/event
               (contact (d/db conn)
                          (:contact/email-address demo-user)))))))))
