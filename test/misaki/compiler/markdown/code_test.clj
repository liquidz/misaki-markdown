(ns misaki.compiler.markdown.code-test
  (:require
    [misaki.compiler.markdown [code :refer :all]]
    [misaki [config :refer [*config*]]
            [tester :refer :all]]
    [clojure [test   :refer :all]
             [string :as str]]))

;; gen-uniq-str
(deftest gen-uniq-str-test
  (is (string? (gen-uniq-str)))
  (is (not= (gen-uniq-str) (gen-uniq-str))))

;; conver-without-code
(defcompilertest convert-without-codes-test
  (let [f #(str/replace % #"a" "A")]

    (testing "code block without language"
      (are [x y] (= x (convert-without-codes f y))
        "AAA"                   ,  "aaa"
        "AAA\nbbb"              ,  "aaa\nbbb"
        "AA```\naaa\n```"       ,  "aa```\naaa\n```"
        "AA```\naaa\nbbb\n```"  ,  "aa```\naaa\nbbb\n```"
        "AA```\naaa\nbbb\n\n```",  "aa```\naaa\nbbb\n\n```"))

    (testing "code block with language"
      (are [x y] (= x (convert-without-codes f y))
        "AA```lang\naaa\n```"       ,  "aa```lang\naaa\n```"
        "AA```lang\naaa\nbbb\n```"  ,  "aa```lang\naaa\nbbb\n```"
        "AA```lang\naaa\nbbb\n\n```",  "aa```lang\naaa\nbbb\n\n```"))

    (testing "multiple code blocks"
      (are [x y] (= x (convert-without-codes f y))
        "```\na\n``` A ```\na\n```"    , "```\na\n``` a ```\na\n```"
        "```\na\n``` A ```lang\na\n```", "```\na\n``` a ```lang\na\n```"))))

;; transform-codes
(defcompilertest transform-codes-test
  (testing "transform code without language"
    (are [x y] (= x (transform-codes y))
      "<pre><code>a</code></pre>"   , "```\na\n```"
      "<pre><code>a</code></pre>"   , "```\n\n\na\n\n\n```"
      "<pre><code>a\nb</code></pre>", "```\na\nb\n```"))

  (testing "transform code with language"
    (are [x y] (= x (transform-codes y))
      "<pre><code class=\"brush: clj;\">a</code></pre>"   , "```clj\na\n```"
      "<pre><code class=\"brush: clj;\">a\nb</code></pre>", "```clj\na\nb\n```"))

  (testing "transform multiple codes"
    (are [x y] (= x (transform-codes y))
      "<pre><code>a</code></pre> b <pre><code>c</code></pre>", "```\na\n``` b ```\nc\n```"
      "<pre><code>a</code></pre> b <pre><code class=\"brush: lang;\">c</code></pre>", "```\na\n``` b ```lang\nc\n```")))


