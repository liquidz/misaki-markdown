{
 ;; directory setting
 :public-dir   "public/"
 :template-dir "template/"
 :post-dir "posts/"
 :layout-dir "layouts/"
 :post-filename-regexp #"(\d{4})-(\d{1,2})-(\d{1,2})[-_](.+)$"
 ;:post-filename-format "$(year)-$(month)/$(filename)"
 :post-filename-format "{{year}}-{{month}}/{{filename}}"

 ;:compile-with-post ["index.html"]

 :url-base "/"
 :posts-per-page 2

 :site {:site-title "misaki markdown"
        :atom       "atom.xml"
        :base-url   "http://localhost:8080"
        :twitter-id "uochan"

        :local {:css ["css/main.css"]
                :js  ["js/highlight.pack.js"
                      "js/main.js"]}
        :remote {:css ["http://fonts.googleapis.com/css?family=Josefin+Sans"
                       "http://yandex.st/highlightjs/7.3/styles/github.min.css"]}}

 ; DEFAULT: FIXME
 :code-regexp #"(?s)```([^\r\n]*)[\r\n]+(.+?)[\r\n]+```"

 ; DEFAULT
 ; <pre><code@(if lang) class="brush: $(lang);"@(end)>$(code)</code></pre>
 :code-html-format "<pre><code@(if lang) class=\"brush: $(lang);\"@(end)>$(code)</code></pre>"

 :compiler "markdown"

 }

