(defproject misaki-markdown "0.0.1-alpha"
  :description "Markdown Compiler for Misaki"
  :url "http://liquidz.githumb.com/misaki/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [misaki "0.2.6.1-beta"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [com.github.rjeschke/txtmark "0.8"]]

  :repositories [["renejeschke-releases" "http://maven.renejeschke.de"]]

  :main misaki.compiler.markdown.core)


