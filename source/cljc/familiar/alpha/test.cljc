
(ns familiar.alpha.test
  "Experimental testing constructs for Clojure(Script).

  ### TEST DOUBLES

  Clojure's `with-redefs` and `with-redefs-fn` provide a foundation for
  injecting function doubles when testing.  `clojure.test` does not, however,
  provide any support for leveraging `with-redef{s,-fn}` to handle multiple
  invocations of a function.

  familiar introduces a lightweight (imposing as few restrictions as possible)
  and pragmatic (acknowledging that it is sometimes desirable to exploit
  `with-redef{s,fn}` when refactoring code into a more testable, injection-based
  form is not) function double construct that attempts to do just that.

  The entry point into this system is the `FnDouble`, an invocable record that
  can be used directly in a `with-redefs` binding as the target function.  But
  the `FnDouble` is really just a dispatcher to a strategy that actually handles
  the how invocations are processed.

  Strategies are thus the mechanism by which the desired testing behaviour can
  be achieved.

  For example, the `FnDoubleSequentialInvocationStategy` validates a particular
  sequence of function invocations.  Invocations are described in the form of a
  predicate and return element, with the predicate being used as an
  \"expectation\" for the invocation.  Both the predicate and the return
  element (if it is a function) are passed a `FnDoubleInvocationData` record,
  through which the invocation arguments may be inspected.

  Other strategies may be implemented to provide matching based on arguments, or
  even a combination of strict sequence and matching; that is: verify
  invocations according to the sequence, unless matched."
  #?(:clj (:require [clojure.test :refer [is]])
     :cljs (:require [clojure.test :refer [is]]
                     [goog.string :as gstring]
                     [goog.string.format])))

#?(:cljs (def format gstring/format))

(defrecord FnDoubleInvocationData
    [fn args])

(defprotocol FnDoubleInvocation
  (invoke [this invocation-data]))

(defrecord FnDoubleSequentialInvocationImpl
    [pred ret]
  FnDoubleInvocation
  (invoke [this invocation-data]
    (let [arg-verification-res (if pred
                                 (is (pred invocation-data)
                                     (format "Unexpected invocation arguments; failed %s"
                                             (pr-str invocation-data)))
                                 true)]
      (cond (not arg-verification-res) nil
            (fn? ret)                  (ret invocation-data)
            :else                      ret))))

(defprotocol FnDoubleSequentialInvocationStategy
  (exhausted [fn-double-sequential-invocation-strategy]
    "Assert (using `clojure.test/is`) that all of the invocations in the
    sequence have been issued; typically called at the end of a test.")
  (exhausted? [fn-double-sequential-invocation-strategy]
    "Return true if all invocations in the sequence were , otherwise false"))

(defrecord FnDoubleSequentialInvocationStategyImpl
  [invocation-instances]
  FnDoubleInvocation
  (invoke [this invocation-data]
    (assert (volatile? invocation-instances))
    (assert (seqable? @invocation-instances))
    (let [invocation-instance (first @invocation-instances)]
      (is invocation-instance (format "Unexpected invocation; invocation-data=%s" (pr-str invocation-data)))
      (vswap! invocation-instances next)
      (when invocation-instance (invoke invocation-instance invocation-data))))
  FnDoubleSequentialInvocationStategy
  (exhausted [this]
    (is (empty? @invocation-instances)))
  (exhausted? [this]
    (exhausted this)))

(defn- -invoke-double
  [invocation-strategy-impl invocation-data & args]
  (assert invocation-strategy-impl "A function double invocation strategy has not been provided")
  (invoke invocation-strategy-impl
          (map->FnDoubleInvocationData (assoc invocation-data :args args))))

#?(:clj (defrecord FnDoubleImpl
            [invocation-strategy-impl opts]
          clojure.lang.IFn
          (invoke [this]
            (-invoke-double invocation-strategy-impl opts))
          (invoke [this arg1]
            (-invoke-double invocation-strategy-impl opts arg1))
          (invoke [this arg1 arg2]
            (-invoke-double invocation-strategy-impl opts arg1 arg2))
          (invoke [this arg1 arg2 arg3]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3))
          (invoke [this arg1 arg2 arg3 arg4]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4))
          (invoke [this arg1 arg2 arg3 arg4 arg5]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5))
          (invoke [this arg1 arg2 arg3 arg4 arg5 arg6]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6))
          (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7))
          (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
          (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
            (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9)))
   :cljs (defrecord FnDoubleImpl
             [invocation-strategy-impl opts]
           IFn
           (-invoke [this]
             (-invoke-double invocation-strategy-impl opts))
           (-invoke [this arg1]
             (-invoke-double invocation-strategy-impl opts arg1))
           (-invoke [this arg1 arg2]
             (-invoke-double invocation-strategy-impl opts arg1 arg2))
           (-invoke [this arg1 arg2 arg3]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3))
           (-invoke [this arg1 arg2 arg3 arg4]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4))
           (-invoke [this arg1 arg2 arg3 arg4 arg5]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5))
           (-invoke [this arg1 arg2 arg3 arg4 arg5 arg6]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6))
           (-invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7))
           (-invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
           (-invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
             (-invoke-double invocation-strategy-impl opts arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))))
