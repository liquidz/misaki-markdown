(ns misaki.compiler.markdown.code
  "Code Parser for Misaki-Markdown"
  (:require
    [misaki.config    :refer [*config*]]
    ;[clostache.parser :refer [render]]
    [cuma.core :refer [render]]
    [clojure.string   :as str]))

; =gen-uniq-str
(defn gen-uniq-str
  "Generate unique string based on (gensym)"
  []
  (str "%" (str/replace (str (gensym)) #"_" ":") "%"))

; =convert-without-codes
(defn convert-without-codes
  "Convert specified string without codes which matches (:code-regexp *config*)."
  [convert-f s]
  (let [code-regexp (:code-regexp *config*)
        code-map    (atom {})
        code->uniq  (fn [[code _]] (let [us (gen-uniq-str)]
                                     (swap! code-map assoc us code) us))
        uniq->code  (fn [res key-str]  (str/replace res key-str (get @code-map key-str)))]
    (-> (str/replace s code-regexp code->uniq)
        convert-f
        (as-> applied-str
              (reduce uniq->code applied-str (keys @code-map))))))

; =transform-codes
(defn transform-codes
  "Transform codes which matches *code-regexp* with *code-html-format*."
  [s]
  (let [{:keys [code-regexp code-html-format]} *config*]
    (str/replace
      s code-regexp
      (fn [[_ lang code]]
        (render code-html-format {:lang (if-not (str/blank? lang) lang) :code code})))))


