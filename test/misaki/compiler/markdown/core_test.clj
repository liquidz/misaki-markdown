(ns misaki.compiler.markdown.core-test
  (:require
    [misaki.compiler.markdown.core :refer :all]
    [misaki [tester  :refer :all]
            [config  :refer [*config*]]]
    [clojure [test   :refer :all]
             [string :as str]]))

(set-base-dir! "test/files/core/")

;;; layout-file?
(defcompilertest layout-file?-test
  (let [a (template-file "layouts/default.html")
        b (template-file "without_layout.html")]
    (is (true?  (layout-file? a)))
    (is (false? (layout-file? b)))))

;;; get-post-data
(defcompilertest get-post-data-test
  (testing "default sort"
    (let [[a b c :as posts] (get-post-data)]
      (are [x y] (= x y)
        3 (count posts)

        "post baz" (:title a)
        "post bar" (:title b)
        "post foo" (:title c)

        "02 Feb 2022" (:date a)
        "01 Jan 2011" (:date b)
        "01 Jan 2000" (:date c)

        "/2022-02/baz.html" (:url a)
        "/2011-01/bar.html" (:url b)
        "/2000-01/foo.html" (:url c)


        "<p>baz</p>" (str/trim (:content a))
        "<p>bar</p>" (str/trim (:content b))
        "<p>foo</p>" (str/trim (:content c))))))

;;; -config
(deftest* -config-test
  (let [config (get-config)]
    (are [x y] (= x y)
      "test/files/core/template/layouts/" (:layout-dir config)
      DEFAULT_CODE_REGEXP (:code-regexp config)
      DEFAULT_CODE_HTML_FORMAT (:code-html-format config))))

;;; -compile
(deftest* -compile-test
  (testing "single html template"
    (let [in  (template-file "without_layout.html")
          out (public-file   "without_layout.html")]
      (is (test-compile in))
      (are [x y] (= x y)
        true (.exists out)
        "<h1>hello</h1><p><strong>world</strong></p>" (str/replace (slurp out) #"[\r\n]" ""))
      (.delete out)))

  (testing "single htm template"
    (let [in  (template-file "htm_extension.htm")
          out (public-file   "htm_extension.htm")]
      (is (test-compile in))
      (are [x y] (= x y)
        true (.exists out)
        "<p>hello world</p>" (str/trim (slurp out)))
      (.delete out)))

  (testing "single md template"
    (let [in  (template-file "md_extension.md")
          out (public-file   "md_extension.html")]
      (is (test-compile in))
      (are [x y] (= x y)
        true (.exists out) ; .md -> .html
        "<p>hello world</p>" (str/trim (slurp out)))
      (.delete out)))

  (testing "template with layout"
    (let [in  (template-file "single_layout.html")
          out (public-file   "single_layout.html")]
      (is (test-compile in))
      (are [x y] (= x y)
        true (.exists out)
        "<p>hello <p>world</p></p>" (str/trim (slurp out)))
      (.delete out)))

  (testing "template under a directory"
    (let [in  (template-file "dir/under_dir.html")
          out (public-file   "dir/under_dir.html")]
      (is (test-compile in))
      (are [x y] (= x y)
        true (.exists out)
        "<p>world</p>" (str/trim (slurp out)))
      (.delete out)
      (.delete (public-file "dir")))))

