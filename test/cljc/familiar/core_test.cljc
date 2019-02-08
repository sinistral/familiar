
(ns familiar.core-test
  (:require [clojure.test  :as test :refer [deftest is testing]]
            [familiar.core :as f]))

(deftest test:format
  (is (= ":foo" (f/format nil "~a" :foo)))
  (is (= ":foo :bar" (f/format nil "~a ~a" :foo :bar))))

(deftest test:fmtstr
  (is (= ":foo" (f/fmtstr "~a" :foo)))
  (is (= ":foo :bar" (f/fmtstr "~a ~a" :foo :bar))))

(deftest test:non-nil!
  (testing "non-nil values are returned, regardless of truthiness"
    (doseq [x [:x false 0]]
      (is (= x (f/non-nil! x)))))
  (testing "assertion errors are raised for nil values"
    (is (thrown?
         #?(:clj  AssertionError :cljs js/Error)
         (f/non-nil! nil)))
    (is (thrown-with-msg?
         #?(:clj  AssertionError :cljs js/Error)
         #"Boom!"
         (f/non-nil! nil "Boom!")))))

(defn- verify-single!-msg
  [coll msg-or-msgs re]
  (is (thrown-with-msg?
        #?(:clj  AssertionError :cljs js/Error)
        re
        (apply f/single! coll msg-or-msgs))))

(defn- test-single-modes
  [<1 =1 >1]
  (is (thrown-with-msg?
        #?(:clj  AssertionError :cljs js/Error)
        #"found an empty collection"
        (f/single! <1)))
  (is (= (first =1) (f/single! =1)))
  (is (thrown-with-msg?
        #?(:clj  AssertionError :cljs js/Error)
        #"found a collection with multiple elements"
        (f/single! >1))))

(deftest test:single
  (letfn []
    (testing "simple collections"
      (test-single-modes [] [1] [1 2 3]))
    (testing "compound collections"
      (test-single-modes {} {:a 1} {:a 1 :b 2}))
    (testing "lazy collections"
      (letfn [(gen-seq [n] (take n (repeatedly rand)))]
        (test-single-modes (gen-seq 0) (gen-seq 1) (gen-seq 3))))
    (testing "custom message: general"
      (verify-single!-msg [] ["foo"] #"found an empty collection; foo:")
      (verify-single!-msg [1 2] ["foo"] #"found a collection with multiple elements; foo:"))
    (testing "custom message: specific"
      (verify-single!-msg [] ["empty" "many"] #": empty:")
      (verify-single!-msg [1 2] ["empty" "many"] #": many:"))))
