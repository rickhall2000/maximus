(ns maximus.client
  (:require [maximus.core :as m]
            [datomic.api :refer [db] :as d]
            [maximus.util :as util :refer [->contact ->event]]
            [clj-time.core :as t]))

#_(m/init! m/uri)
(def conn (m/connect! m/uri))

(defn active-count []
  (time
   (count (m/active-cards (db conn)))))
