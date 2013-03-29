{
 ;; directory setting
 :public-dir   "public/"
 :template-dir "template/"
 :post-dir "posts/"
 :layout-dir "layouts/"
 :post-filename-regexp #"(\d{4})-(\d{1,2})-(\d{1,2})[-_](.+)$"
 :post-filename-format "{{year}}-{{month}}/{{filename}}"

 :url-base "/hello/"

 :compiler "markdown"

 :posts-per-page 2
 }

