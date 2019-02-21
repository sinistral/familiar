
(ns familiar.test
  "Extensions to Clojure(Script)'s unit testing framework.

  ### EXTENSIONS TO `clojure.test/is`

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
    (throw (ex-info nil {:text \"text\"})))

  ### FIXTURES

  Fixtures allow one to run code before and after tests, to set up the
  context in which tests should be run.  A fixture is just a function
  that returns a function that calls the function passed as an argument; i.e.:
  ```
  (defn- fx0
    [t]
    #(do (setup)
         (t)
         (teardown)))
  ```
  Please note that familiar fixtures are subtly different to clojure.test
  fixtures in that they must *return* a function; in this way they are more
  like Ring middleware, which is a natural expression of functionality that
  wraps testing.

  This distinction allows fixtures to be parameterizable without
  sacrificing composability, e.g. the fixture:
  ```
  (defn- fx1
    [x t]
    #(do (setup x)
         (t)))
  ```
  can be parameterised on a per-test basis using something like:
  ```
  (testing \"...\"
    (with-fixtures [(fx0) (fx1 ::foo)]
      (is (= ::foo (test-subject ...)))))
  ```
  which allows fixtures to succinctly express variance in test conditions
  without having to resort to dynamic vars for that configuration."
  #?(:cljs (:require-macros [familiar.test.assertions]
                            [familiar.test :refer [with-fixtures]]))
  #?(:clj  (:require [clojure.test :as test]
                     [familiar.test.assertions])
     :cljs (:require [clojure.test :as test])))

(defmacro with-fixtures
  "Run a test body wrapped in fixtures, presumably to establish the testing
  context.  `fxs` is a vector of fixture specs; each is:
  ```
  (fixture-name fixture-arg0 fixture-arg1)
  ```
  Please see the namespace documentation for details."
  [fxs test-body]
  (letfn [(make-partial [[name & args]]
            (list 'apply 'partial name (or (conj args 'list) [])))]
    (let [aggregate-form (map make-partial fxs)]
      `(((comp ~@aggregate-form)
         (fn [] ~test-body))))))
