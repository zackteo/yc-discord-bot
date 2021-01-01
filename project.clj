(defproject prayer-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[dk.ative/docjure "1.14.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.suskalo/discljord "1.1.1"]]
  :min-lein-version "2.0.0"
  :uberjar-name "prayer-bot.jar"
  :repl-options {:init-ns prayer-bot.core}
  :profiles {:uberjar {:aot :all}}
  :main prayer-bot.core)
