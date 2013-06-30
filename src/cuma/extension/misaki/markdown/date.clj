(ns cuma.extension.misaki.markdown.date
  (:require
    [cuma.extension.date :refer [date-format]]
    [clojure.string :as str]))

(defn date->xml-schema
  [data date]
  (date-format data date "yyyy-MM-dd'T'HH:mm:ss"))

