(ns cuma.extension.misaki.markdown.date
  "Extensions for cuma to convert dates."
  (:require
    [cuma.extension.date :refer [date-format]]
    [clojure.string :as str]))

(defn date->xml-schema
  "Convert date to xml-schema."
  [data date]
  (date-format data date "yyyy-MM-dd'T'HH:mm:ss"))

