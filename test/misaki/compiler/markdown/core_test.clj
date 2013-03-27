(ns misaki.compiler.markdown.core-test
  (:use clojure.test
        [misaki.compiler.markdown core template]
        misaki.tester
        [misaki.config :only [*config*]])
  (:require [clojure.string  :as str]))

; set base directory which include _config.clj
; default testing base directory is "test"
(set-base-dir! "test/")

(defcompilertest layout-file?-test
  (let [a (template-file "_layouts/without_layout.html")
        b (template-file "single.html")]
    (is (true?  (layout-file? a)))
    (is (false? (layout-file? b)))))


;;; get-template-option
(defcompilertest get-template-option-test
  (testing "with option"
    (let [data   (slurp (template-file "option/with_option.html"))
          option (get-template-option data)]
      (are [x y] (= x y)
        "sample title" (:title option)
        "default"      (:layout option)
        "false"        (:html? option)
        nil            (:unknown option))))

  (testing "no option"
    (let [data   (slurp (template-file "option/without_option.html"))
          option (get-template-option data)]
      (are [x y] (= x y)
        {} option
        {} (get-template-option "")
        {} (get-template-option nil)))))



;;; remove-option-lines
(defcompilertest remove-option-lines-test
  (testing "with option"
    (let [body (slurp (template-file "option/with_option.html"))]
      (is (= "hello" (str/trim (remove-option-lines body))))))

  (testing "no option"
    (let [body (slurp (template-file "option/without_option.html"))]
      (is (= "hello" (str/trim (remove-option-lines body)))))))

;;; load-layout
(deftest* load-layout-test
  (binding [*config* (get-config)]
    (is (= "hello {{&content}}"
           (str/trim (remove-option-lines (load-layout "without_layout")))))))

;;; replace-code-block
;(deftest* replace-code-block-test
;  (testing "without language specified"
;    (are [x y] (= x (replace-code-block y))
;      "<pre><code>hello</code></pre>"
;      "```\nhello\n```"
;      "<pre><code>hello</code></pre>"
;      "```\n\nhello\n\n```"
;      "<pre><code>he\nll\no</code></pre>"
;      "```\nhe\nll\no\n```"))
;
;  (testing "with language specified"
;    (are [x y] (= x (replace-code-block y))
;      "<pre><code class=\"brush: html;\">hello</code></pre>"
;      "```html\nhello\n```"
;      "<pre><code class=\"brush: clojure;\">hello</code></pre>"
;      "```clojure\n\nhello\n\n```"
;      "<pre><code class=\"brush: markdown;\">he\nll\no</code></pre>"
;      "```markdown\nhe\nll\no\n```"))
;
;  (testing "code should be encoded"
;    (are [x y] (= x (replace-code-block y))
;      "<pre><code>&lt;hello&gt;</code></pre>"
;      "```\n<hello>\n```"
;      "<pre><code>&quot;hello&quot;</code></pre>"
;      "```\n\"hello\"\n```")))

;;; get-tempaltes
(defcompilertest get-templates-test
  (testing "single layout"
    (let [data  (slurp (template-file "layout/single_layout.html"))
          tmpls (get-templates data)]
      (are [x y] (= x y)
        2 (count tmpls)
        "world" (-> tmpls first first str/trim)
        "hello {{&content}}" (-> tmpls second first str/trim)
        "world" (-> tmpls first  second :title)
        "hello" (-> tmpls second second :title))))

  (testing "multi layout"
    (let [data  (slurp (template-file "layout/multi_layout.html"))
          tmpls (get-templates data)]
      (are [x y] (= x y)
        3 (count tmpls)
        "foo" (-> tmpls first first str/trim)
        "baz {{&content}}" (-> tmpls second first str/trim)
        "hello {{&content}}" (-> tmpls (nth 2) first str/trim)
        "foo"   (-> tmpls first  second :title)
        "baz"   (-> tmpls second second :title)
        "hello" (-> tmpls (nth 2) second :title)))))


;;; html-template?
(defcompilertest html-template?-test
  (testing "HTML template should be true"
    (are [x] (true? (html-template? x))
      "<span>hello</span>"
      "<span>\nhello\n</span>"
      "<p>hello <span>world</span></p>"))

  (testing "Non HTML template should be false"
    (are [x] (false? (html-template? x))
      "hello"
      "hello > world"
      "hello < world"
      "<span>hello")))

;;; render*
(defcompilertest render*-test
  (testing "markdown template without markdown? option"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello {{msg}}" {}] {:msg "world"})))))

  (testing "html template without markdown? option"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<h1>hello {{msg}}</h1>" {}] {:msg "world"})))))

  (testing "markdown template with TRUE markdown? option"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello {{msg}}" {:markdown? "true"}] {:msg "world"})))))

  (testing "html template with TRUE markdown? option"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<h1>hello {{msg}}</h1>" {:markdown? "true"}] {:msg "world"})))))

  (testing "markdown template with FALSE markdown? option"
    (is (= "#hello world"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello {{msg}}" {:markdown? "false"}] {:msg "world"})))))

  (testing "html template with FALSE markdown? option"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<h1>hello {{msg}}</h1>" {:markdown? "false"}] {:msg "world"}))))))

;;; render-template
(defcompilertest render-template-test
  (testing "with layout, no variable"
    (let [file (template-file "layout/single_layout.html")]
      (is (= "<p>hello <p>world</p></p>"
             (str/trim (render-template file {}))))))

  (testing "with layout, no variable, not allow layout"
    (let [file (template-file "layout/single_layout.html")]
      (is (= "<p>world</p>"
             (str/trim (render-template file {} :allow-layout? false))))))

  (testing "with self variable"
    (let [file (template-file "layout/layout_with_self_var.html")]
      (is (= "<p>hello <p>neko world</p></p>"
             (str/trim (render-template file {}))))
      (is (= "<p>hello <p>neko world inu</p></p>"
             (str/trim (render-template file {:msg " inu"}))))))

  (testing "with self variable, not allow layout"
    (let [file (template-file "layout/layout_with_self_var.html")]
      (is (= "<p>neko world</p>"
             (str/trim (render-template file {} :allow-layout? false))))))

  (testing "with layout variable"
    (let [file (template-file "layout/layout_with_layout_var.html")]
      (is (= "<p>hello <p>hello world</p></p>"
             (str/trim (render-template file {}))))
      (is (= "<p>hello <p>hello world inu</p></p>"
             (str/trim (render-template file {:msg " inu"}))))))

  (testing "with layout variable, not allow layout"
    (let [file (template-file "layout/layout_with_layout_var.html")]
      (is (= "<p>hello world</p>"
             (str/trim (render-template file {} :allow-layout? false))))))

  (testing "html template (:markdown? option is false)"
    (let [file (template-file "html/self_html.html")]
      (is (= "<p>hello world</p>"
             (str/trim (render-template file {}))))))

  (testing "html template (without :markdown? option)"
    (let [file (template-file "html/self_html_without_md_opt.html")]
      (is (= "<p>hello <span>world</span></p>"
             (str/trim (render-template file {}))))))

  (testing "template with html layout (:markdown? option is false)"
    (let [file (template-file "html/layout_html.html")]
      (is (= "hello <p>world</p>"
             (str/trim (render-template file {}))))))

  (testing "template with html layout (without :markdown? option)"
    (let [file (template-file "html/layout_html_without_md_opt.html")]
      (is (= "<div>hello <p>world</p></div>"
             (str/trim (render-template file {}))))))

  (testing "no layout"
    (let [file (template-file "option/without_option.html")]
      (is (= "<p>hello</p>"
             (str/trim (render-template file {}))))
      (is (= "<p>hello</p>"
             (str/trim (render-template file {} :allow-layout? false)))))))



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
      "test/template/_layouts/" (:layout-dir config)
      10                        (:post-entry-max config))))

;;; -compile
(deftest* -compile-test
  (testing "single html template"
    (let [in  (template-file "single.html")
          out (public-file   "single.html")]
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
    (let [in  (template-file "layout/single_layout.html")
          out (public-file   "layout/single_layout.html")]
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
        "<p>hello <p>world</p></p>" (str/trim (slurp out)))
      (.delete out))))



