(ns boo.core
  (:require [clojure-http.resourcefully :as res]
            [clojure-http.client]
            [clojure.xml]
            [clojure.zip]
            [clojure.contrib.zip-filter.xml :as zf]))

(set! *print-length* 103)
(set! *print-level*    5)

(def uri "http://rest.bioontology.org/obs/ontologies")

(defn zip-xml [uri]
  (clojure.zip/xml-zip (clojure.xml/parse uri)))

(defn map-from-bean [ontology-bean]
  (into {} (map 
             (fn [attr] [attr (zf/xml1-> ontology-bean attr zf/text)]) 
             [:name :localOntologyId :virtualOntologyId])))

(defn get-ontologies []
  (let [parsed         (zip-xml uri)
        ontology-beans (zf/xml-> parsed :data :list :ontologyBean)]
    (into [] (map 
               (fn [b] (map-from-bean b))
               ontology-beans))))

(defn main []
  (get-ontologies))

(main)