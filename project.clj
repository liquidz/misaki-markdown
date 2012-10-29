(defproject misaki-markdown "0.0.1-alpha"
  :description "Markdown Compiler for Misaki"
  :url "http://liquidz.githumb.com/misaki/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [misaki "0.2.4-beta"]
                 [de.ubercode.clostache/clostache "1.3.0"]
                 [markdown-clj "0.9.9"]]

  :main misaki.compiler.markdown.core)


