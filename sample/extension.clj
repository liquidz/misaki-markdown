(ns cuma.extension.misaki.markdown.custom
  "User Custom Extension for cuma."
  (:require [clojure.string :as str]))

;; example
(defn hello
  [data s]
  (str/upper-case (str "hello " s "!!!")))

