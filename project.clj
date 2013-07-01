(defproject misaki-markdown "0.0.1-beta"
  :description "Markdown Compiler for Misaki"
  :url "http://liquidz.githumb.com/misaki-markdown/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [misaki "0.2.6.2-beta"]
                 [cuma "0.0.4"]
                 [com.github.rjeschke/txtmark "0.8"]]
  :dev-dependencies [clj-time "0.5.0"]
  :plugins [[codox "0.6.4"]]

  :repositories [["renejeschke-releases" "http://maven.renejeschke.de"]]

  :codox {:src-dir-uri "http://github.com/liquidz/misaki-markdown/blob/master"
          :src-linenum-anchor-prefix "L"
          :output-dir "doc"}


  :main misaki.compiler.markdown.core)


