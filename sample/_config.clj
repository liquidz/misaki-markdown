{
 ;; directory setting
 :public-dir   "public/"
 :template-dir "template/"
 :post-dir "posts/"
 :layout-dir "layouts/"
 :post-filename-regexp #"(\d{4})-(\d{1,2})-(\d{1,2})[-_](.+)$"
 :post-filename-format "{{year}}-{{month}}/{{filename}}"

 :url-base "/hello/"
 :posts-per-page 2

 :site {:site-title "misaki markdown"
        :local {:css ["css/main.css"]
                :js  ["js/highlight.pack.js"
                      "js/main.js"]}
        :remote {:css ["http://fonts.googleapis.com/css?family=Josefin+Sans"
                       "http://yandex.st/highlightjs/7.3/styles/github.min.css"]}
        }

 :compiler "markdown"

 }

