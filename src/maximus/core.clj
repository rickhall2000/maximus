(ns maximus.core
  (:require [clojure.java.io :as io]
            [datomic.api :refer [db q] :as d]))

(def uri "datomic:sql://hello?jdbc:postgresql://localhost:5432:datomic?user=datomic&password=datomic")

(def +schema-file+ "schema.dtm")
(def +seed-file+ "seed.dtm")

(defn read-file
  [f]
  (-> f
      io/resource
      slurp
      read-string))

(defn transact-with-creator!
  [conn creator transaction]
  @(d/transact conn
               (conj transaction
                     {:db/id #db/id [:db.part/tx -1]
                      :transaction/user creator})))
(defn seed-db!
  [conn]
  @(d/transact conn (read-file +schema-file+))
  #_(transact-with-creator! conn "inital seed" (read-file +seed-file+)))


(defn connect! [uri]
  (d/connect uri))

(defn init!
  [uri]
  (d/delete-database uri)

  (d/create-database uri)
  (def conn (connect! uri))
  (seed-db! conn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CRUD

(defn all
  [db q]
  (reduce (fn [coll e]
            (conj coll (d/touch (d/entity db e))))
          []
          (flatten (seq q))))

(defn all-contacts
  [db]
  (all db
       (d/q '[:find ?e
              :where [?e :contact/email-address]]
            db)))

(defn all-events
  [db]
  (all db
       (d/q '[:find ?e
              :where [?e :event/type]]
            db)))

(defn all-user-tx
  [db user]
  (all db
       (d/q '[:find ?e
              :in $ ?user
              :where
              [?e :transaction/user ?user]]
            db
            user)))

(defn active-cards [db]
  (all db
       (d/q '[:find ?e
              :where [?e :contact/status :contact.status/active]]
            db)))

(defn contact
  "find user by email"
  [db email]
  (first (all db
              (d/q '[:find ?e
                     :in $ ?email
                     :where [?e :contact/email-address ?email]]
                   db
                   email))))

(defn store-contact!
  [conn creator m]
  (transact-with-creator! conn creator
   [(merge {:db/id #db/id [:db.part/user -2]
            :contact/status :contact.status/active}
           m)]))

(defn store-event!
  [conn creator {contact :db/id} m]
  (transact-with-creator! conn creator
   [(merge m {:db/id #db/id [:db.part/user -2]})
    [:db/add contact :contact/event #db/id [:db.part/user -2]]]))

(defprotocol Update!
  (assert! [entity conn creator attrib value]
    "Given a datomic.query.EntityMap, datomic connection, creator, attrib and value,
    assert the new value in the database.")
  (retract! [entity conn creator attrib value]
    "Given a datomic.query.EntityMap, datomic connection, creator, attrib and value,
    retract the value from the database."))

(extend-protocol Update!
  datomic.query.EntityMap
  (assert! [this conn creator attrib value]
    (transact-with-creator! conn creator [[:db/add (:db/id this) attrib value]]))
  (retract! [this conn creator attrib value]
    (transact-with-creator! conn creator [[:db/retract (:db/id this) attrib value]]))

  java.lang.Long
  (assert! [this conn creator attrib value]
    (transact-with-creator! conn creator [[:db/add this attrib value]]))
  (retract! [this conn creator attrib value]
    (transact-with-creator! conn creator [[:db/retract this attrib value]])))


;;;;;;;;;;;;;;;;;;;;
;; Audit trail

(defn audit-log
  ([conn] (audit-log conn nil nil))
  ([conn start] (audit-log conn start nil))
  ([conn start end]
     (let [db (db conn)]
       (->> (d/tx-range (d/log conn) start end)
            (mapcat :data)
            (reduce (fn [datom-maps [eid aid value tid assert?]]
                      (conj datom-maps
                            {:entity eid
                             :attribute (:db/ident (d/entity db aid))
                             :value value
                             :transaction tid
                             :assert? assert?
                             :user (:transaction/user (d/entity db tid))}))
                    [])))))


;;;;;;;;;;;;;;;;;;;;
;; Add attributes

(def db-types
  #{"keyword" "string" "boolean"
    "long" "bigint" "float"
    "double" "bigdec" "ref"
    "instant" "uuid" "uri" "bytes"})

(defn ->schema-attribute
  [ns name type many?]
  (let [card (if (#{"many"} many?) :db.cardinality/many :db.cardinality/one)
        type (or (get db-types type)
                 (throw (ex-info "bad attribute type" {:data type})))]
    [{:db/id (d/tempid :db.part/db)
      :db/ident (keyword ns name)
      :db/valueType (keyword "db.type" type)
      :db/cardinality card
      :db.install/_attribute :db.part/db}]))

(def contact-attrib (partial ->schema-attribute "contact"))
(defn contact-attributes
  [db]
  (reduce (fn [acc val] (concat acc val))
          []
          (q '[:find ?attr
               :where
               [_ :db/ident ?attr]
               [(namespace ?attr) ?namespace]
               [(= ?namespace "contact")]]
             db)))
(defn add-contact-attrib!
  [conn creator name type many?]
  (transact-with-creator! conn creator
                          (contact-attrib name type many?)))

(comment
  (def uri "datomic:free://localhost:4334/hello")
  (def uri "datomic:mem://hello")

  (do ;; shutdown-restart-reseed
    (d/delete-database uri)

    (d/create-database uri)
    (def conn (d/connect uri))
    (seed-db! conn))

  (d/q '[:find ?e ?name
         :where [?e :contact/creator ?name]]
       (d/db conn))


  (d/touch (d/entity (d/db conn)
                     (ffirst (d/q '[:find ?e
                                    :where [?e :contact/creator "rick astley"]]
                                  (d/db conn)))))

  (assert! 17592186045419 conn "brian" :contact/email-address "foo@somewhere.over.the.rainbow.com")
  )
