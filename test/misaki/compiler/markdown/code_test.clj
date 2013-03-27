(ns misaki.compiler.markdown.code-test
  (:require
    [misaki.compiler.markdown [code :refer :all]]
    [misaki [config :refer [*config*]]
            [tester :refer :all]
            ]
    [clojure.test :refer :all]
    [clojure.string :as str]))


;; gen-uniq-str
(deftest gen-uniq-str-test
  (is (string? (gen-uniq-str)))
  (is (not= (gen-uniq-str) (gen-uniq-str))))

;; conver-without-code
(defcompilertest convert-without-codes-test
  (let [f #(str/replace % #"a" "A")]

    (are [x y] (= x (convert-without-codes f y))
      "AAA" "aaa"
      "AAA\nbbb" "aaa\nbbb"
      "AA```\naaa\n```"  "aa```\naaa\n```"
      "AA```\naaa\nbbb\n```"  "aa```\naaa\nbbb\n```"
      "AA```\naaa\nbbb\n\n```"  "aa```\naaa\nbbb\n\n```")

    (are [x y] (= x (convert-without-codes f y))
      "AA```lang\naaa\n```" "aa```lang\naaa\n```")

    (are [x y] (= x (convert-without-codes f y))
      "```\na\n``` A ```\na\n```" "```\na\n``` a ```\na\n```")
    ))

;; transform-codes
(defcompilertest transform-codes-test
  (are [x y] (= x (transform-codes y))
    "<pre><code>a</code></pre>" "```\na\n```"
    "<pre><code>a</code></pre>" "```\n\n\na\n\n\n```"
    "<pre><code>a\nb</code></pre>" "```\na\nb\n```"
    )

  (are [x y] (= x (transform-codes y))
    "<pre><code class=\"brush: clj;\">a</code></pre>" "```clj\na\n```"
    "<pre><code class=\"brush: clj;\">a\nb</code></pre>" "```clj\na\nb\n```"
    )
  )


