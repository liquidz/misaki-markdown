(ns misaki.compiler.markdown.template
  "Markdown Template Parser for Misaki"
  (:require
    [misaki.compiler.markdown.code :refer :all]
    [misaki.util [file   :refer :all]
                 [date   :refer :all]
                 [string :refer :all]]
    [misaki.config    :refer [*config*]]
    [clostache.parser :refer [render]]
    [clojure.string   :as str])
  (:import
    [com.github.rjeschke.txtmark Processor]))

; =parse-option-line
(defn- parse-option-line
  [line]
  (re-seq #"^;+\s*@([\w?]+)\s+(.+)$" line))

; =get-template-option
(defn get-template-option
  "Get template option from slurped template file."
  [slurped-data]
  (if (string? slurped-data)
    (let [lines  (map str/trim (str/split-lines slurped-data))
          params (remove nil? (map parse-option-line lines))]
      (into {} (for [[[_ k v]] params] [(keyword k) v])))
    {}))

; =remove-option-lines
(defn remove-option-lines
  "Remove option lines from slurped template file."
  [slurped-data]
  (let [lines  (map str/trim (str/split-lines slurped-data))]
    (str/join "\n" (remove #(parse-option-line %) lines))))

; =remove-useless-html-lines
(defn remove-useless-html-lines
  "Remove useless empty lines in HTML."
  [s]
  (-> s
    (str/replace #"(<[^/]+?>)[\r\n]*" (fn [[_ tag]] tag))
    (str/replace #"[\r\n]*(</.+?>)" (fn [[_ tag]] tag))))

; =load-layout
(defn load-layout
  "Load layout file and return slurped data."
  [layout-name]
  (slurp (path (:layout-dir *config*) (str layout-name ".html"))))

; =get-templates
(defn get-templates
  "Get slurped template file containing layout files."
  [slurped-data]
  (letfn [(split [s] ((juxt remove-option-lines get-template-option) s))]
    (take-while
      #(not (nil? %))
      (iterate (fn [[_ tmpl-option]]
                 (if-let [layout-name (:layout tmpl-option)]
                   (split (load-layout layout-name))))
               (split slurped-data)))))

; =html-template?
(defn html-template?
  "Check whether slurped data is HTML or not."
  [slurped-data]
  (not (nil? (re-seq #"(?s)<.+?>.*</.+?>" slurped-data))))

; =render*
;   markdown-flag?( ) html-template?(T) => html
;   markdown-flag?( ) html-template?(F) => markdown
;   markdown-flag?(T) html-template?(T) => markdown
;   markdown-flag?(T) html-template?(F) => markdown
;   markdown-flag?(F) html-template?(T) => html
;   markdown-flag?(F) html-template?(F) => html
(defn- render* [[body option :as template] data]
  (let [render-result (render body data)
        md-flag (:markdown? option "noopt")
        markdown-process? (case md-flag "true" true, "false" false
                                        "noopt" (not (html-template? body)))]
    (if markdown-process?
      (transform-codes
        (convert-without-codes #(Processor/process %) render-result))
      render-result)))

; =render-template
(defn render-template
  "Render java.io.File as HTML."
  [file base-data & {:keys [allow-layout?]
                     :or   {allow-layout? true}}]
  (let [tmpls (get-templates (slurp file))
        htmls (map first tmpls)
        data  (merge base-data (reduce merge (reverse (map second tmpls))))]

    (if allow-layout?
      (reduce
        (fn [result-html tmpl-html]
          (if tmpl-html
            (render* tmpl-html (merge data {:content (str/trim result-html)}))
            result-html))
        (render* (first tmpls) data)
        (rest tmpls))
      (render* (first tmpls) data))))


