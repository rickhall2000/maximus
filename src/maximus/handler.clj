(ns maximus.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.xml :as xml]
            [maximus.reportdata :as reportdata]
            [ring.util.response :as res]))

(def xml-contact
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
    <contact>
      <first-name>\"Elvis\"</first-name>
      <last-name>\"Presley\"</last-name>
      <created-on>#inst \"2014-06-10T08:09:15.885-00:00\"</created-on>
      <email-address>\"king@lasvegas.com\"</email-address>
      <postal-address>\"The end of lonely street\"</postal-address>
      <status>:contact.status/active</status>
      <lead-source>\"somesite.com\"</lead-source>
      <phone-number>\"1238887890\"</phone-number>
   </contact>")

(def xml-stuff "
<books>
  <book>
     <title>The Old Man and the Sea</title>
     <author>Ernest Hemingway</author>
  </book>
</books>")


(def xml-two
  "<books>
<book>
     <title>The Old Man and the Sea</title>
     <author>Ernest Hemingway</author>
  </book>
  <book>
    <title>Foo</title>
    <author>Bar</author>
    <isdn>some random isdn thing</isdn>
  </book>
</books>
")

(def simple-edn
  {:books
   [{:title "The Sun Also Rises"
     :author "Ernest Hemingway"}]
   })

(defn return-xml [req]
  {:status 200 :content-type "text/xml"
   :body xml-two})


(defn return-edn [req]
  {:status 200 :content-type "application/edn"
   :body (pr-str simple-edn)})

(defn edn-to-xml [stuff]
  (xml/parse stuff))

(defn return-xml2 [req]
  {:status 200 :content-type "text/xml"
   :body (edn-to-xml simple-edn)})

(defroutes app-routes
  (GET "/" [] return-xml)
  (GET "/edn" [] return-edn)
  (GET "/xml" [] return-xml2)
  (GET "/contacts" [] (-> (res/response (reportdata/guestcards-response))
                            (res/content-type "text/xml")))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
