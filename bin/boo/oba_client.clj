(ns boo.oba-client
  ; :use should warn if :as is given - it's meaningless.
  (:require [clojure-http.resourcefully :as res]
            [clojure-http.client]
            [clojure.xml])
     (:use  [clojure.contrib.string :only [join]]))

; Set a high default timeout, as the service sometimes takes awhile to respond.
; Use the production service endpoint URI.
(def default-options 
  {:timeout 30 :uri "http://rest.bioontology.org/obs/annotator"})

; Parameters the annotator accepts. Any one not in this list (excluding
; textToAnnotate) is not valid.
(def all-parameters #{
  :email
  :filterNumber
  :format
  :isStopWordsCaseSensitive
  :isVirtualOntologyID
  :levelMax
  :longestOnly
  :ontologiesToExpand
  :ontologiesToKeepInResult
  :mappingTypes
  :minTermSize
  :scored
  :semanticTypes
  :stopWords
  :wholeWordOnly
  :withDefaultStopWords
  :withSynonyms})

(defn parse [body-seq]
  (let [xml (join "" (seq body-seq))
       parsed (clojure.xml/parse (java.io.ByteArrayInputStream. (.getBytes xml)))]
    (println (subs xml 0 10))))

(defn execute-request [uri timeout parse-xml parameters text]
    (binding [clojure-http.client/*connect-timeout* timeout]
      (let [parameters (assoc parameters :textToAnnotate text)
            response (res/post uri {} parameters)] 
        (if parse-xml 
          (parse (response :body-seq))
          (response :body-seq)))))

(defn get-ontology-options [options]
  "If the :ontologies pseudo-parameter is given, set ontologiesToExpand and
   ontologiesToKeepInResult accordingly. These will be joined as strings if 
   necesesary later."
  (if-let [ontologies (options :ontologies)]
    (assoc options 
      :ontologiesToExpand ontologies 
      :ontologiesToKeepInResult ontologies)
    options))

(defn join-vectors [parameters]
  "Return a {k0, v0, k1, v1} mapping where all vs that are vectors are replaced
   by (join ',' v)."
  (into {} (map 
            (fn [[k, v]] [k (if (vector? v) (join "," v) v)]) 
            parameters)))

(defn init-annotator
  "Return a partial with the given fn option and annotator parameters."
  ([] (init-annotator {}))
  ([options] 
    (let [{:keys [uri timeout parse-xml]} (merge default-options options)
          options    (get-ontology-options options)
          parameters (select-keys (join-vectors options) all-parameters)]
      (partial execute-request uri timeout parse-xml parameters))))

(defn annotate-text []
  (println ((init-annotator {:parse-xml true}) "cancer is a disease of neoplasm disorder.")))

(annotate-text)