
(ns familiar.alpha.core-test
  (:refer-clojure :exclude [cond cond-> cond->> format let])
  #?(:clj (:require [clojure.test :refer [deftest is testing]]
                    [familiar.alpha.core :refer :all])
     :cljs (:require [clojure.test :refer [deftest is testing]]))
  #?(:cljs (:require-macros [familiar.alpha.core :refer [cond cond-> cond->> let]])))

(deftest let-bindings
  (testing "basic form"
    (is (= [:a :b]
           (let [[x :a]
                 [y :b]]
             [x y]))))
  (testing "multi-form body"
    (is (= [:a :b]
           (let [[x :a]
                 [y :b]]
             (+ 0 1 2)
             [x y])))))

(deftest cond-clauses
  (letfn [(select [x]
            (cond [(nil? x)
                   :foo]
                  [(= "bar" x)
                   :bar]
                  [:else
                   :baz]))]
    (is (= :foo (select nil)))
    (is (= :bar (select "bar")))
    (is (= :baz (select 'else)))))

(deftest cond-thread-clauses
  (testing "->"
    (letfn [(thread-> [x]
              (cond->
                  x
                [(let [[y (:foo x)]] (and (int? y) (even? y)))
                 (update :foo inc)]
                [(let [[y (:bar x)]] (and (int? y) (even? y)))
                 (update :bar inc)]))]
      (is (= {:foo 3}
             (thread-> {:foo 2})))
      (is (= {:foo 1 :bar 3}
             (thread-> {:foo 1 :bar 2})))
      (is (= {:foo 3 :bar 9}
             (thread-> {:foo 2 :bar 8})))))
  (testing "->>"
    (letfn [(thread->> [x]
              (cond->>
                  x
                [(let [[y (:foo x)]] (and (int? y) (even? y)))
                 (#(update % :foo inc))]
                [(let [[y (:bar x)]] (and (int? y) (even? y)))
                 (#(update % :bar inc))]))]
      (is (= {:foo 3}
             (thread->> {:foo 2})))
      (is (= {:foo 1 :bar 3}
             (thread->> {:foo 1 :bar 2})))
      (is (= {:foo 3 :bar 9}
             (thread->> {:foo 2 :bar 8}))))))
