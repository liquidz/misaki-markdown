(ns misaki.compiler.markdown.core-test
  (:require
    [misaki.compiler.markdown.core :refer :all]
    [misaki [tester  :refer :all]
            [config  :refer [*config*]]]
    [clj-time.core   :refer [date-time]]
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
  (testing "posts with pagination"
    (let [[a :as posts] (get-post-data)]
      (are [x y] (= x y)
        1                    (count posts)
        "post baz"           (:title a)
        (date-time 2022 2 2) (:date a)
        "/2022-02/baz.html"  (:url a)
        "<p>baz</p>"         (str/trim (:content a)))))

  (testing "default sort, all posts"
    (let [[a b c :as posts] (get-post-data :all? true)]
      (are [x y] (= x y)
        3 (count posts)

        "post baz" (:title a)
        "post bar" (:title b)
        "post foo" (:title c)

        (date-time 2022 2 2) (:date a)
        (date-time 2011 1 1) (:date b)
        (date-time 2000 1 1) (:date c)

        "/2022-02/baz.html" (:url a)
        "/2011-01/bar.html" (:url b)
        "/2000-01/foo.html" (:url c)

        "<p>baz</p>" (str/trim (:content a))
        "<p>bar</p>" (str/trim (:content b))
        "<p>foo</p>" (str/trim (:content c))))))

;;; make-filename
(defcompilertest make-filename-test
  (are [x y] (= (.getName (public-file x))
                (#'misaki.compiler.markdown.core/make-filename (template-file y)))
    "index.html" "index.html"
    "index.htm" "index.htm"
    "index.xml" "index.xml"
    "index.html" "index.md"))

;; make-base-site-data
(defcompilertest make-base-site-data-test
  (let [site (make-base-site-data)]
    (are [x y] (= x y)
      "/"   (:root site)
      nil   (:next-page site)
      nil   (:prev-page site)
      1     (count (:posts site))
      3     (count (:all-posts site))
      "bar" (:foo site)))

  (bind-config [:url-base "/foo/"
                :next-page "n", :prev-page "p"]
    (let [site (make-base-site-data)]
      (are [x y] (= x y)
        "/foo/" (:root site)
        "n"     (:next-page site)
        "p"     (:prev-page site)))))

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
      (.delete (public-file "dir"))))

  (testing "post(only prev) template"
    (let [in       (post-file "2000-01-01-foo.html")
          out      (public-file "2000-01/foo.html")
          prev-out (public-file "2011-01/bar.html")]
      (is (test-compile in))
      (is (.exists out))
      (is (.exists prev-out))
      (let [arr (str/split (str/trim (slurp out)) #"[\r\n]+")]
        (are [x y] (= x y)
          2                      (count arr)
          "<p>foo</p>"           (nth arr 0)
          "<p>prev=post bar</p>" (nth arr 1)))
      (.delete out)
      (.delete prev-out)
      (.delete (public-file "2000-01"))
      (.delete (public-file "2011-01"))
      ))

  (testing "post(prev and next) template"
    (let [in       (post-file "2011-01-01-bar.html")
          out      (public-file "2011-01/bar.html")
          prev-out (public-file "2022-02/baz.html")
          next-out (public-file "2000-01/foo.html")]
      (is (test-compile in))
      (is (.exists out))
      (is (.exists prev-out))
      (is (.exists next-out))
      (let [arr (str/split (str/trim (slurp out)) #"[\r\n]+")]
        (are [x y] (= x y)
          3                      (count arr)
          "<p>bar</p>"           (nth arr 0)
          "<p>prev=post baz</p>" (nth arr 1)
          "<p>next=post foo</p>" (nth arr 2)))
      (.delete out)
      (.delete prev-out)
      (.delete next-out)
      (.delete (public-file "2000-01"))
      (.delete (public-file "2011-01"))
      (.delete (public-file "2022-02"))))

  (testing "post(only next) template"
    (let [in       (post-file "2022-02-02-baz.html")
          out      (public-file "2022-02/baz.html")
          next-out (public-file "2011-01/bar.html")]
      (is (test-compile in))
      (is (.exists out))
      (is (.exists next-out))
      (let [arr (str/split (str/trim (slurp out)) #"[\r\n]+")]
        (are [x y] (= x y)
          2                      (count arr)
          "<p>baz</p>"           (nth arr 0)
          "<p>next=post bar</p>" (nth arr 1)))
      (.delete out)
      (.delete next-out)
      (.delete (public-file "2011-01"))
      (.delete (public-file "2022-02")))))

