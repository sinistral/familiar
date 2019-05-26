
(def source-paths #{"source/cljc" "source/clj"})

(set-env! :resource-paths
          source-paths
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
            [cider/cider-nrepl           "0.21.1"       :scope "test"]
            [cider/piggieback            "0.4.1"        :scope "test"]]
          :repositories
          #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(require '[clojure.string              :as str :refer [trim]]
         '[adzerk.boot-test            :as clj-test]
         '[cljs.repl.node              :as cljs-repl]
         '[cider.piggieback            :as piggieback :refer [cljs-repl]]
         '[codox.boot                  :as codox]
         '[crisptrutski.boot-cljs-test :as cljs-test])

(def project 'familiar)
(def version (trim (slurp "VERSION")))

(task-options!
 pom {:project     project
      :version     version
      :description "A Clojure(Script) companion library."
      ; :url         "http://example/FIXME"
      :scm         {:url "https://github.com/sinistral/familiar"}
      :license     {"2-Clause BSD License"
                    "https://opensource.org/licenses/BSD-2-Clause"}})

(declare build-jar)

(defn start-cljs-repl
  []
  (cljs-repl (cljs-repl/repl-env)))

(deftask test-clj
  []
  (clj-test/test))

(deftask test-cljs
  []
  (cljs-test/test-cljs :exit? true :js-env :node))

(deftask test-all
  []
  (comp (test-clj)
        (test-cljs)))

(deftask install-jar
  []
  (comp (build-jar)
        (install)))

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

(deftask build-all
  []
  (comp (test-all)
        (build-jar)
        (build-doc)
        (target)))

;; ------------------------------------------------------------------------- ;;

(deftask deploy-snapshot
  []
  (comp (test-all)
        (build-jar)
        (push :repo "clojars"
              :gpg-sign true
              :gpg-user-id (System/getenv "CLOJARS_GPG_KEY_ID")
              :ensure-snapshot true)))

(deftask deploy-release
  []
  (comp (test-all)
        (build-jar)
        (push :repo "clojars"
              :gpg-sign true
              :gpg-user-id (System/getenv "CLOJARS_GPG_KEY_ID")
              :ensure-release true)))
