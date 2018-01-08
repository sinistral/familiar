
(ns familiar.test
  "Extensions to Clojure(Script)'s unit testing framework.

  EXTENSIONS TO `clojure.test/is`

  There are special assertions for testing Clojure(Script) exceptions that have
  been created via \"ex-info\".  The \"(is (ex-thrown? ...))\" form tests if an
  exception of type ExceptionInfo is thrown, abstracting away the specific type
  differences between Clojure and ClojureScript:

  (is (ex-thrown? (throw (ex-info \"test\" {}))))

  \"(is (ex-thrown-with-data? f ...))\" does the same thing, and also tests the
  data in the exception (as returned by \"ex-data\") by applying it to the
  function \"f\":

  (ex-thrown-with-data?
    #(clojure.core.match/match % {:text \"text\"} true :else false)
    (throw (ex-info nil {:text \"text\"})))"
  #?(:cljs (:require-macros [familiar.test.assertions]))
  #?(:clj  (:require [clojure.test :as test]
                     [familiar.test.assertions])
     :cljs (:require [clojure.test :as test])))
