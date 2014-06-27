(ns maximus.util
  (:require [clojure.data.xml :as data]
            [clojure.xml :as xml]))

(defn ->contact
  [first last email postal phone lead-source created-on]
  {:contact/first-name first
   :contact/last-name last
   :contact/email-address email
   :contact/postal-address postal
   :contact/phone-number phone
   :contact/status :contact.status/active
   :contact/lead-source lead-source
   :contact/created-on created-on})

(defn ->event
  [type agent]
  {:event/type type, :event/agent agent})

(defn map->xml
  [parent-key m]
  (if (instance? datomic.query.EntityMap m)
    (recur parent-key (into {} m))
    (reduce-kv (fn [m k v]
               (->> (if (satisfies? data/EventGeneration v)
                      v
                      (clojure.string/replace (pr-str v)
                                              #"(#inst \")|(\"$)" ""))
                    (data/element k {})
                    (update-in m [:content] conj)))
             (data/element parent-key {} []) m)))

(defn element-response
  ([content] (element-response :results content))
  ([tag content]
     (data/emit-str (data/element tag {} content))))
