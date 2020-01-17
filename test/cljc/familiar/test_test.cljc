
(ns familiar.test-test
  #?(:cljs (:require-macros [familiar.test-test :refer [with-test-report]]))
  (:require [clojure.test        :as test :refer [deftest is testing]]
            [clojure.core.match  :as match]
            [clojure.string      :as string]

            [familiar.test       :as ft]
            [familiar.alpha.test :as fta]))

#?(:clj (defmacro with-test-report
          [name expr tests]
          `(let [~name (volatile! nil)]
             (binding [test/report (fn [e#] (vreset! ~name e#))]
               ~expr)
             ~@tests)))

(deftest test:ex-thrown?
  (testing "fail if no exception is thrown by form"
    (with-test-report r
      (is (ex-thrown? true))
      ((is (= :fail (:type @r)))
       (is (= 'clojure.core/ex-info (:expected @r)))
       (is (= nil (:actual @r)))
       (is (= nil (:message @r))))))
  (testing "fail with messsage if no exception is thrown"
    (with-test-report r
      (is (ex-thrown? true) "--message--")
      ((is (= "--message--" (:message @r))))))
  (testing "error if thrown exception is not an ex-info"
    (with-test-report r
      (is (ex-thrown? (throw #?(:clj (RuntimeException.) :cljs (js/Error.)))))
      ((is (= :error (:type @r))))))
  (testing "pass when ex-info is thrown"
    (with-test-report r
      (is (ex-thrown? (throw (ex-info "test" {}))))
      ((is (= :pass (:type @r)))
       (is (= 'clojure.core/ex-info (:expected @r)))
       (is (= 'clojure.core/ex-info (:actual @r)))
       (is (= nil (:message @r))))))
  (testing "sanity check"
    (is (ex-thrown? (throw (ex-info "test" {}))))))

(deftest test:ex-thrown-with-data?
  (testing "fail if exception does not contain expected data"
    (with-test-report r
      (is (ex-thrown-with-data?
           #(match/match % {:text "text"} true :else false)
           (throw (ex-info nil {:text "something else"}))))
      ((is (= :fail (:type @r))))))
  (testing "fail if actual exception data does not match expected"
    (with-test-report r
      (is (ex-thrown-with-data?
           #(match/match % {:text (x :guard (fn [s] (string/includes? s "bar")))} true :else false)
           (throw (ex-info nil {:text "foo"}))))
      ((is (= :fail (:type @r))))))
  (testing "pass if actual exception data matches expected"
    (with-test-report r
      (is (ex-thrown-with-data?
           #(match/match % {:text "text"} true :else false)
           (throw (ex-info nil {:text "text"}))))
      ((is (= :pass (:type @r))))))
 (testing "sanity check"
    (is (ex-thrown-with-data?
         #(match/match % {:text "text"} true :else false)
         (throw (ex-info nil {:text "text"}))))))

(defn- fx0
  [t]
  #(t))

(def ^:dynamic *fixture-effects* nil)

(defn- fx1
  [x t]
  (vswap! *fixture-effects* conj x)
  #(t))

(defn- fxf
  [f t]
  (vswap! *fixture-effects* #(map f %))
  #(t))

(deftest test:with-fixtures
  (testing "test bodies are executed"
    (let [test-effect (volatile! nil)]
      (ft/with-fixtures [(fx0)]
        (vreset! test-effect ::yay))
      (is (= ::yay @test-effect))))
  (testing "an empty list of fixtures is permissable (but not recommended)"
    (is (nil? (ft/with-fixtures [] nil))))
  (testing "fixtures can be parameterised"
    (binding [*fixture-effects* (volatile! '())]
      (ft/with-fixtures [(fx1 0) (fx1 1) (fx1 2)]
        (is (= [0 1 2] @*fixture-effects*)))))
  (testing "fixture params can be fns"
    (binding [*fixture-effects* (volatile! '())]
      (ft/with-fixtures [(fxf inc) (fx1 1)]
        (is (= [2] @*fixture-effects*))))))

(defn- a-fn [] 0)

(deftest test:fn-double
  (testing "doubles can be invoked"
    (let [fn-double (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity ::doppelganger)])
                          fta/->FnDoubleSequentialInvocationStategyImpl
                          (fta/->FnDoubleImpl {:fn a-fn}))]
        (is (= ::doppelganger (fn-double 0))))))

(deftest test:fn-double-sequential-strategy
  (testing "sequential fn doubles pass expected invocations"
    (let [fn-double (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity ::doppelganger)])
                        fta/->FnDoubleSequentialInvocationStategyImpl
                        (fta/->FnDoubleImpl {:fn a-fn}))]
      (is (= ::doppelganger (fn-double 0)))))
  (testing "sequential fn doubles fail on unexpected invocations"
    (with-test-report r
      (let [fn-double (-> (volatile! [])
                          fta/->FnDoubleSequentialInvocationStategyImpl
                          (fta/->FnDoubleImpl {:fn a-fn}))]
        (fn-double 0))
      ((is (= :fail (:type @r))))))
  (testing "sequential fn doubles fail on unmatched args"
    (with-test-report r
      (let [fn-double (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl (constantly false) ::doppelganger)])
                          fta/->FnDoubleSequentialInvocationStategyImpl
                          (fta/->FnDoubleImpl {:fn a-fn}))]
        (fn-double 0))
      ((is (= :fail (:type @r))))))
  (testing "sequential fn doubles don't return the expected result on an unmatched invocation"
    (let [fn-double (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl (constantly false) ::doppelganger)])
                        fta/->FnDoubleSequentialInvocationStategyImpl
                        (fta/->FnDoubleImpl {:fn a-fn}))]
      (with-test-report r
        (is (nil? (fn-double 0)))
        ())))
  (testing "ret is invoked as a fn"
    (let [fn-double (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity #(-> % :args first inc))])
                        fta/->FnDoubleSequentialInvocationStategyImpl
                        (fta/->FnDoubleImpl {:fn a-fn}))]
      (is (= 100 (fn-double 99)))))
  (testing "sequential fn doubles pass expected invocations"
    (let [seq-strategy (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity ::doppelganger)])
                           fta/->FnDoubleSequentialInvocationStategyImpl)
          fn-double    (fta/->FnDoubleImpl seq-strategy {:fn a-fn})]
      (fn-double 0)
      (is (fta/exhausted? seq-strategy))))
  (testing "sequential fn doubles fail when all invocations are not consumed"
    (testing "query"
      (let [seq-strategy (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity ::doppelganger)])
                             fta/->FnDoubleSequentialInvocationStategyImpl)
            fn-double    (fta/->FnDoubleImpl seq-strategy {:fn a-fn})]
        (with-test-report r
          (is (fta/exhausted? seq-strategy))
          ((is (= :fail (:type @r)))))))
    (testing "assert"
      (let [seq-strategy (-> (volatile! [(fta/->FnDoubleSequentialInvocationImpl identity ::doppelganger)])
                             fta/->FnDoubleSequentialInvocationStategyImpl)
            fn-double    (fta/->FnDoubleImpl seq-strategy {:fn a-fn})]
        (with-test-report r
          (fta/exhausted seq-strategy)
          ((is (= :fail (:type @r)))))))))
