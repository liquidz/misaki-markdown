{
 ;; directory setting
 :public-dir   "public/"
 :template-dir "template/"
 :compiler "markdown"
 :post-dir "_posts/"
 :layout-dir "_layouts/"
 :post-filename-regexp #"(\d{4})-(\d{1,2})-(\d{1,2})[-_](.+)$"
 :post-filename-format "{{year}}-{{month}}/{{filename}}"
 }

