(ns maximus.pgdata
  (:require [clojure.java.jdbc :as j]))

(def dbinfo' {:subprotocol "postgresql"
             :subname "//localhost:5432/notdatomic"
             :user "notdatomic"
             :password "notdatomic"
             :classname "org.postgresql.Driver"})

(def dbinfo {:connection (j/get-connection dbinfo')})

(def lead-status
  {:active 1
   :expired 2
   :sold 3
   :lost 4})

(defn store-contact! [dbinfo creator contact] ;; check sig
  (j/insert! dbinfo :contact {:first (:contact/first-name contact)
                                :last (:contact/last-name contact)
                                :createdon (:contact/first-name contact)
                                :email (:contact/email-address contact)
                                :postal (:contact/postal-address contact)
                                :phone (:contact/phone-number contact)
                                :leadsource (:contact/lead-source contact)
                                :status 1
                                :createdby creator}))

(defn active-count [dbinfo]
  (j/query dbinfo ["select count(rowid) from contact where status = ?" 1]))

(defn active-cards [dbinfo]
  (j/query dbinfo ["select * from contact where status = ?" 1]))

(defn store-event! [dbinfo creator contact event]
  (j/insert! dbinfo :eventx {:contactid (:rowid contact)
                             :eventtype (name (:event/type event))
                             :createdon (:createdon contact)
                             :createdby creator
                             :agent (:event/agent agent)}))

;; this only works for status and lead source, I should fix that later
(defn update-contact! [contact dbinfo agent field new-val]
  (let [rowid (:rowid contact)
        old-val (field contact)
        fieldname (name field)
        insert-stmt (str "")]
    (if (= fieldname "status")
      (j/update! dbinfo :contact {:status 2} ["rowid = ?" rowid])
      (j/update! dbinfo :contact {:leadsource new-val} ["rowid = ?" rowid]))
    (j/insert! dbinfo :cardchange {:cardid rowid :fieldname fieldname
                                   :oldval old-val :newval new-val
                                   :createdon (:contact/created-on contact)
                                   :createdby agent})))
