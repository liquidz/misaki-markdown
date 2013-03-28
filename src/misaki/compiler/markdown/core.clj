(ns misaki.compiler.markdown.core
  "Markdown Compiler for Misaki"
  (:require
    [misaki.compiler.markdown.template :refer :all]
    [misaki.util [file   :refer :all]
                 [date   :refer :all]
                 [string :refer :all]]
    [misaki [config :refer [*config*] :as cnf]
            [core   :as msk]
            [server :as srv]]
    [clojure.string :as str]))

;; ## Default Values
(def DEFAULT_CODE_REGEXP
  "Default regexp for matching code blocks."
  #"(?s)```([^\r\n]*)[\r\n]+(.+?)[\r\n]+```")

(def DEFAULT_CODE_HTML_FORMAT
  "Default format to render code blocks."
  "<pre><code{{#lang}} class=\"brush: {{lang}};\"{{/lang}}>{{code}}</code></pre>")

;; Default post entry max
(def POST_ENTRY_MAX 10)

; =layout-file?
(defn layout-file?
  "Check whether specified file is layout file or not."
  [file]
  (if-let [layout-dir (:layout-dir *config*)]
    (str-contains? (.getAbsolutePath file) layout-dir)
    false))

; =get-post-data
(defn get-post-data
  "Get posts data."
  []
  (map #(let [date (cnf/get-date-from-file %)]
          (assoc (-> % slurp get-template-option)
                 :date (date->string date)
                 :date-xml-schema (date->xml-schema date)
                 :content (render-template % (:site *config*) :allow-layout? false)
                 :url (cnf/make-output-url %)))
       (msk/get-post-files :sort? true)))

; =make-filename
(defn- make-filename
  "Make output filename from java.io.File."
  [file]
  (let [filename (cnf/make-output-filename file)]
    (if (has-extension? :md file)
      (str (remove-extension filename) ".html")
      filename)))

; =make-base-site-data
(defn make-base-site-data
  "Make base site data for rendering templates."
  []
  (let [posts (get-post-data)
        date  (now)]
    (merge (:site *config*)
           {:date      (date->string date)
            :org-date  date
            :date-xml-schema (date->xml-schema date)
            :posts     (take (:post-entry-max *config*) posts)
            :all-posts posts})))


;; ## Compiler Functions
;; See document "[Develop Compiler](http://liquidz.github.com/misaki/toc/07-develop-compiler.html)"

; =-extension
(defn -extension
  "Specifying file extensions function called by misaki.core."
  []
  (list :htm :html :md))






; =-config
(defn -config
  "Custom configuration function called by misaki.core."
  [{:keys [template-dir] :as config}]
  (assoc config
         :layout-dir (path template-dir (:layout-dir config))
         :code-regexp (:code-regexp config DEFAULT_CODE_REGEXP)
         :code-html-format (:code-html-format config DEFAULT_CODE_HTML_FORMAT)
         :post-entry-max (:post-entry-max config POST_ENTRY_MAX)))

; =-compile
(defn -compile
  "Compile function called by misaki.core."
  [config file]
  (binding [*config* config]
    (if (layout-file? file)
      {:status 'skip :all-compile? true}
      {:status   true
       :filename (make-filename file)
       :body     (render-template file (make-base-site-data))})))


(defn -main [& args]
  (apply srv/-main args))
