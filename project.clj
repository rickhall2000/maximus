(defproject maximus "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-ring "0.8.10"]]

  :ring {:handler maximus.handler/app}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-pro "0.9.4766.16"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [compojure "1.1.6"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [clj-time "0.7.0"]]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
