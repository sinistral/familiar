
(ns familiar.alpha.test
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
  (exhausted [fn-double-sequential-invocation-strategy])
  (exhausted? [fn-double-sequential-invocation-strategy]))

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
