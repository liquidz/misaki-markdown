(ns misaki.compiler.markdown.template-test
  (:require
    [misaki.compiler.markdown.template :refer :all]
    [misaki [tester  :refer :all]
            [config  :refer [*config*]]]
    [clojure [test   :refer :all]
             [string :as str]]))

(set-base-dir! "test/files/template/")

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
    (is (= "hello $(raw content)"
           (str/trim (remove-option-lines (load-layout "without_layout")))))))

;;; get-tempaltes
(defcompilertest get-templates-test
  (testing "single layout"
    (let [data  (slurp (template-file "layout/single_layout.html"))
          tmpls (get-templates data)]
      (are [x y] (= x y)
        2 (count tmpls)
        "world" (-> tmpls first first str/trim)
        "hello $(raw content)" (-> tmpls second first str/trim)
        "world" (-> tmpls first  second :title)
        "hello" (-> tmpls second second :title))))

  (testing "multi layout"
    (let [data  (slurp (template-file "layout/multi_layout.html"))
          tmpls (get-templates data)]
      (are [x y] (= x y)
        3 (count tmpls)
        "foo" (-> tmpls first first str/trim)
        "baz $(raw content)" (-> tmpls second first str/trim)
        "hello $(raw content)" (-> tmpls (nth 2) first str/trim)
        "foo"   (-> tmpls first  second :title)
        "baz"   (-> tmpls second second :title)
        "hello" (-> tmpls (nth 2) second :title)))))

;;; render*
(defcompilertest render*-test
  (testing "markdown template without markdown? option should be rendered as MARKDOWN"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello $(msg)" {}] {:msg "world"})))))

  (testing "html template without markdown? option should be rendered as MARKDOWN"
    (is (= "<p><strong>hello world</strong></p>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<strong>hello $(msg)</strong>" {}] {:msg "world"})))))

  (testing "markdown template with TRUE markdown? option should be renderd as MARKDOWN"
    (is (= "<h1>hello world</h1>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello $(msg)" {:markdown? "true"}] {:msg "world"})))))

  (testing "html template with TRUE markdown? option should be rendered as MARKDOWN"
    (is (= "<p><strong>hello world</strong></p>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<strong>hello $(msg)</strong>" {:markdown? "true"}] {:msg "world"})))))

  (testing "markdown template with FALSE markdown? option should be rendered as HTML"
    (is (= "#hello world"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["#hello $(msg)" {:markdown? "false"}] {:msg "world"})))))

  (testing "html template with FALSE markdown? option should be rendered as HTML"
    (is (= "<strong>hello world</strong>"
           (str/trim
             (#'misaki.compiler.markdown.template/render*
                 ["<strong>hello $(msg)</strong>" {:markdown? "false"}] {:msg "world"})))))

  (testing "custom extension should be work correctly."
    (is (= "hello world"
           (#'misaki.compiler.markdown.template/render*
             ["$(hello msg)" {:markdown? "false"}] {:msg "world"})))

    (is (= "WORLD"
           (#'misaki.compiler.markdown.template/render*
             ["$(upper msg)" {:markdown? "false"}] {:msg "world"})))))

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
      (is (= "<p>hello <p><span>world</span></p></p>"
             (str/trim (render-template file {}))))))

  (testing "template with html layout (:markdown? option is false)"
    (let [file (template-file "html/layout_html.html")]
      (is (= "hello <p>world</p>"
             (str/trim (render-template file {}))))))

  (testing "template with html layout (without :markdown? option)"
    (let [file (template-file "html/layout_html_without_md_opt.html")]
      (is (= "<div>hello <p>world</p></div>"
             (str/trim (render-template file {}))))))

  (testing "template with html and markdown layout"
    (let [file (template-file "md_and_html/md_template_with_site_var.html")]
      (is (= "hello <h1>markdown</h1><p><em>world</em></p>"
             (str/replace (render-template file (:site *config*)) #"[\r\n]" "")))))

  (testing "no layout"
    (let [file (template-file "option/without_option.html")]
      (is (= "<p>hello</p>"
             (str/trim (render-template file {}))))
      (is (= "<p>hello</p>"
             (str/trim (render-template file {} :allow-layout? false)))))))
