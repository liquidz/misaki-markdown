(ns misaki.compiler.markdown.core
  "Markdown Compiler for Misaki"
  (:require
    [misaki.compiler.markdown.template :refer :all]
    [misaki.util [file     :refer :all]
                 [date     :refer :all]
                 [string   :refer :all]
                 [sequence :refer [get-prev-next]]]
    [misaki [config :refer [*config* *base-dir*] :as cnf]
            [core   :as msk]
            [server :as srv]]
    [clojure.string :as str]))

(declare make-base-site-data)

;; ## Default Values
(def DEFAULT_LAYOUT_DIR
  "Default layout directory."
  "layouts/")
(def DEFAULT_CODE_REGEXP
  "Default regexp for matching code blocks."
  #"(?s)```([^\r\n]*)[\r\n]+(.+?)[\r\n]+```")
(def DEFAULT_CODE_HTML_FORMAT
  "Default format to render code blocks."
  "<pre><code@(if lang) class=\"brush: $(lang);\"@(end)>$(code)</code></pre>")

; =layout-file?
(defn layout-file?
  "Check whether specified file is layout file or not."
  [file]
  (if-let [layout-dir (:layout-dir *config*)]
    (str-contains? (.getAbsolutePath file) layout-dir)
    false))

; =md-extension->html-extension
(defn- md-extension->html-extension
  "Convert markdown extension(*.md) to html extension(*.html)."
  [s]
  (if (has-extension? :md s)
    (str (remove-last-extension s) ".html")
    s))

; =make-url
(def ^{:private true} make-url
  "Make output url from java.io.File."
  (comp md-extension->html-extension cnf/make-output-url))

; =make-filename
(def ^{:private true} make-filename
  "Make output filename from java.io.File."
  (comp md-extension->html-extension cnf/make-output-filename))

; =load-extension-files
(defn- load-extension-files
  "Load cuma extension files."
  [file]
  (cond
    (string? file)     (load-file (path *base-dir* file))
    (sequential? file) (doseq [f file] (load-extension-files f))))


; =get-post-data
(defn get-post-data
  "Get posts data."
  [& {:keys [all?] :or {all? false}}]
  (let [site (make-base-site-data :ignore-post? true)]
      (map #(let [date (cnf/get-date-from-file %)]
              (assoc (-> % slurp get-template-option)
                     :file %
                     :date date
                     :content (render-template % (merge (:site *config*) site)
                                               :allow-layout? false
                                               :skip-runtime-exception? true)
                   :url (make-url %)))
         (msk/get-post-files :sort? true :all? all?))))

; =make-base-site-data
(defn make-base-site-data
  "Make base site data for rendering templates."
  [& {:keys [ignore-post?] :or {ignore-post? false}}]
  (let [date (now)]
    (merge (:site *config*)
           {:date      (date->string date)
            :root      (:url-base *config*)
            :next-page (:next-page *config*)
            :prev-page (:prev-page *config*)
            :posts     (if-not ignore-post? (get-post-data))
            :all-posts (if-not ignore-post? (get-post-data :all? true))
            :date-xml-schema (date->xml-schema date)})))


;; ## Compiler Functions

; =-extension
(defn -extension
  "Specifying file extensions function called by misaki.core."
  []
  (list :htm :html :md :xml))

; =-config
(defn -config
  "Custom configuration function called by misaki.core."
  [{:keys [template-dir] :as config}]

  ;; load extension
  (when-let [ext-file (some-> config :cuma :extension)]
    (load-extension-files ext-file))

  (assoc config
         :layout-dir       (path template-dir (:layout-dir config DEFAULT_LAYOUT_DIR))
         :code-regexp      (:code-regexp config DEFAULT_CODE_REGEXP)
         :code-html-format (:code-html-format config DEFAULT_CODE_HTML_FORMAT)))

; =-compile
(defn -compile
  "Compile function called by misaki.core."
  [config file]
  (binding [*config* config]
    (cond
      ; layout template
      (layout-file? file)
      {:status 'skip, :all-compile? true}

      ; post template
      (cnf/post-file? file)
      (let [site        (make-base-site-data)
            [prev next] (get-prev-next #(= file (:file %)) (:all-posts site))
            site        (assoc site :prev prev :next next)]
        ; compile neighbor
        (when (= :single (:-compiling config))
          (if prev (msk/compile* {:-compiling :neighbor} (:file prev)))
          (if next (msk/compile* {:-compiling :neighbor} (:file next))))
        {:status true, :filename (make-filename file)
         :body (render-template file site)})

      ; other templates
      :else
      {:status true, :filename (make-filename file)
       :body (render-template file (make-base-site-data))})))

(defn -main [& args]
  (apply srv/-main args))
