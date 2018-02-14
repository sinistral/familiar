
(def project 'familiar)
(def version "0.1.0-SNAPSHOT")

(def source-paths #{"source/cljc" "source/clj" "source/cljs"})

(set-env! :resource-paths
          (conj source-paths "resource")
          :source-paths
          #{"test/cljc"}
          :dependencies
          '[[org.clojure/clojure         "1.9.0"        :scope "provided"]
            [org.clojure/core.match      "0.3.0-alpha5" :scope "provided"]
            [org.clojure/clojurescript   "1.9.946"      :scope "provided"]
            ;; clj dev dependencies
            [adzerk/boot-test            "RELEASE"      :scope "test"]
            ;; dev dependencies
            [boot-codox                  "0.10.3"       :scope "test"]
            ;; cljs dev dependencies
            [adzerk/boot-cljs            "2.1.4"        :scope "test"]
            [crisptrutski/boot-cljs-test "0.3.4"        :scope "test"]
            ;; REPL dependencies.
            [com.cemerick/piggieback     "0.2.1"        :scope "test"]
            [org.clojure/tools.nrepl     "0.2.12"       :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "A Clojure(Script) companion library."
      ; :url         "http://example/FIXME"
      :scm         {:url "https://github.com/sinistral/familiar"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(require '[adzerk.boot-test            :as clj-test]
         '[cemerick.piggieback         :as piggieback]
         '[cljs.repl.node              :as cljs-repl]
         '[codox.boot                  :as codox]
         '[crisptrutski.boot-cljs-test :as cljs-test])

(swap! boot.repl/*default-middleware* conj 'cemerick.piggieback/wrap-cljs-repl)

(defn start-cljs-repl
  []
  (piggieback/cljs-repl (cljs-repl/repl-env)))

(deftask test-clj
  []
  (clj-test/test))

(deftask test-cljs
  []
  (cljs-test/test-cljs :exit? true :js-env :node))

;; ------------------------------------------------------------------------- ;;

(deftask build-doc
  []
  (comp (codox/codox
         :name (str project)
         :version (str version)
         :source-paths source-paths)))

(deftask build-jar
  []
  (comp (pom) (jar)))

(deftask install-jar
  []
  (comp (build-jar)
        (install)))

(deftask build-all
  []
  (comp (test-clj)
        (test-cljs)
        (build-jar)
        (build-doc)
        (target)
        (install)))
