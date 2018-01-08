
(ns familiar.test.assertions
  (:require [familiar.core :refer [fmtstr]]))

(defn ^{:private true} test-sym
  [sym runtime]
  (symbol (str runtime) sym))

(defmulti ex-type identity)

(defmethod ex-type 'clojure.test
  [_]
  'clojure.lang.ExceptionInfo)

(defmethod ex-type 'cljs.test
  [_]
  'cljs.core.ExceptionInfo)

(defn ^{:private true} match-ex-info
  "Generates the form used by `assert-expr`, `clojure.test`'s extension point
  for new assertions."
  [test-expr match-fn msg runtime]
  `(try
     ~@test-expr
     (~(test-sym "do-report" runtime)
      {:type     :fail
       :message  ~msg
       :expected 'ex-info
       :actual   nil})
     (catch ~(ex-type runtime) e#
       (~(test-sym "do-report" runtime)
        {:type     :pass
         :message  ~msg
         :expected 'ex-info
         :actual   'ex-info})
       (if (nil? ~match-fn)
         (do
           (~(test-sym "do-report" runtime)
            {:type     :pass
             :message  ~msg
             :expected 'ex-info
             :actual   'ex-info})
           e#)
         (let [report# {:message  ~msg
                        :expected (quote ~match-fn)
                        :actual   (ex-data e#)}]
           (if (apply ~match-fn [(ex-data e#)])
             (do
               (~(test-sym "do-report" runtime) (assoc report# :type :pass))
               e#)
             (~(test-sym "do-report" runtime) (assoc report# :type :fail))))))))

;;; Install the assertions.  We use `eval` so to avoid resolving the namespace.
;;; By only attempting to resolve at run-time and not compile-time, we can trap
;;; the exception raised should the namespace not exist (i.e. `cljs.test` in
;;; Clojure-only projects).

(eval
 (letfn [(try-req [ns-sym]
           (try
             (require [ns-sym])
             true
             (catch java.io.FileNotFoundException e
               false)))
         (clj-test-def []
           (let [runtime 'clojure.test]
             (when (try-req runtime)
               `(do
                  (defmethod ~(test-sym "assert-expr" runtime) (quote ~(symbol "ex-thrown?"))
                    [msg# form#]
                    (let [expr# (next form#)]
                      (match-ex-info expr# nil msg# (quote ~runtime))))
                  (defmethod ~(test-sym "assert-expr" runtime) (quote ~(symbol "ex-thrown-with-data?"))
                    [msg# form#]
                    (let [expr#     (nthnext form# 1)
                          check-fn# (first (next form#))]
                      (match-ex-info expr# check-fn# msg# (quote ~runtime))))))))
         (cljs-test-def []
           (let [runtime 'cljs.test]
             (when (try-req runtime)
               `(do
                  (defmethod ~(test-sym "assert-expr" runtime) (quote ~(symbol "ex-thrown?"))
                    [_# msg# form#]
                    (let [expr# (next form#)]
                      (match-ex-info expr# nil msg# (quote ~runtime))))
                  (defmethod ~(test-sym "assert-expr" runtime) (quote ~(symbol "ex-thrown-with-data?"))
                    [_# msg# form#]
                    (let [expr#     (nthnext form# 1)
                          check-fn# (first (next form#))]
                      (match-ex-info expr# check-fn# msg# (quote ~runtime))))))))]
   (let [method-defs (keep identity [(clj-test-def) (cljs-test-def)])]
     `(do ~@method-defs))))
