
(ns familiar.core-test
  (:require [clojure.test  :as test :refer [deftest is testing]]
            [familiar.core :as f]))

(deftest test:format
  (is (= ":foo" (f/format nil "~a" :foo)))
  (is (= ":foo :bar" (f/format nil "~a ~a" :foo :bar))))

(deftest test:fmtstr
  (is (= ":foo" (f/fmtstr "~a" :foo)))
  (is (= ":foo :bar" (f/fmtstr "~a ~a" :foo :bar))))
