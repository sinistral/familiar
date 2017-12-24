
(def project 'familiar)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths
          #{"resource" "source/cljc"}
          :source-paths
          #{"test/cljc"}
          :dependencies
          '[[org.clojure/clojure         "1.9.0"]
            [org.clojure/clojurescript   "1.9.946" :scope "test"]
            ;; clj dependencies
            [adzerk/boot-test            "RELEASE" :scope "test"]
            ;; cljs dependencies
            [adzerk/boot-cljs            "2.1.4"   :scope "test"]
            [crisptrutski/boot-cljs-test "0.3.4"   :scope "test"]
            ;; REPL dependencies.
            [com.cemerick/piggieback     "0.2.1"   :scope "test"]
            [org.clojure/tools.nrepl     "0.2.12"  :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "A Clojure(Script) companion library."
      ; :url         "http://example/FIXME"
      :scm         {:url "https://github.com/sinistral/familiar"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build-jar
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-test            :as clj-test]
         '[crisptrutski.boot-cljs-test :as cljs-test])

(deftask test-clj
  []
  (clj-test/test))

(deftask test-cljs
  []
  (cljs-test/test-cljs :exit? true :js-env :node))
