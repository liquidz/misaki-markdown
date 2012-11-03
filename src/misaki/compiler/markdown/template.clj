(ns misaki.compiler.markdown.template
  "Markdown Template Parser for Misaki"
  (:use
    [misaki.util file date string]
    [misaki.config    :only [*config*]]
    [clostache.parser :only [render]]
    ;[markdown         :only [md-to-html-string]]
    )
  (:require
    [clojure.string :as str])
  (:import
    [com.github.rjeschke.txtmark Processor])
  )


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


; =replace-code-block
(defn replace-code-block
  "Replace code block to HTML based on SyntaxHighlighter."
  [s]
  (str/replace
    s #"(?s)```([^\r\n]*)\n+(.+?)[\r\n]*```"
    (fn [[_ brush codes]]
      (let [klass (if-not (str/blank? brush)
                    (str " class=\"brush: " brush ";\""))]
        (str "<pre><code" klass ">" (escape-string codes) "</code></pre>")))))


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
;   opt( ) html(T) => html
;   opt( ) html(F) => markdown
;   opt(T) html(T) => markdown
;   opt(T) html(F) => markdown
;   opt(F) html(T) => html
;   opt(F) html(F) => html
(defn- render* [[body option :as template] data]
  (let [body (replace-code-block body)
        render-result (render body data)
        md-flag (:markdown? option "noopt")]
      (case md-flag
        "true"  (Processor/process render-result)
        "false" render-result
        "noopt" (if (html-template? body)
                  render-result
                  (Processor/process render-result)))))

; =render-template
(defn render-template
  "Render java.io.File as HTML."
  [file base-data & {:keys [allow-layout?]
                     :or   {allow-layout? true}}]
  (let [tmpls (get-templates (slurp file))
        htmls (map first tmpls)
        data  (merge base-data (reduce merge (reverse (map second tmpls))))]

   ; (remove-useless-html-lines
      (if allow-layout?
        (reduce
          (fn [result-html tmpl-html]
            (if tmpl-html
              (render* tmpl-html (merge data {:content (str/trim result-html)}))
              result-html))
          (render* (first tmpls) data)
          (rest tmpls))
        (render* (first tmpls) data))
   ;   )
    ))


