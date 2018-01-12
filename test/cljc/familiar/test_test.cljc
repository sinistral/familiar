
(ns familiar.test-test
  #?(:cljs (:require-macros [familiar.test-test :refer [with-test-report]]))
  (:require [clojure.test       :as test :refer [deftest is testing]]
            [clojure.core.match :as match]
            [clojure.string     :as string]
            [familiar.test      :as ft]))

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