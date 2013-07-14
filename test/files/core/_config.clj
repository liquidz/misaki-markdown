{
 :public-dir   "public/"
 :template-dir "template/"
 :compiler "markdown"
 :post-dir "posts/"
 :layout-dir "layouts/"
 :post-filename-regexp #"(\d{4})-(\d{1,2})-(\d{1,2})[-_](.+)$"
 :post-filename-format "{{year}}-{{month}}/{{filename}}"

 ; FOR TESTING
 :posts-per-page 1
 :site { :foo "bar" }
 :cuma { :extension "extension" }
 }

