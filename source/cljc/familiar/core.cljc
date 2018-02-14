
(ns familiar.core
  #?(:clj (:refer-clojure :exclude [format]))
  (:require [clojure.pprint :refer [cl-format]]))

(defn format
  "Replaces `clojure.core/format` with `clojure.pprint/cl-format`.  Takes the
  same arguments as [cl-format]; for details please refer to
  the [documentation][cl-format] for that function.
  [cl-format]: https://clojure.github.io/clojure/clojure.pprint-api.html#clojure.pprint/cl-format"
  [writer format-in & args]
  (apply cl-format writer format-in args))

(defn fmtstr
  "Provides a marginally more convenient form of `clojure.pprint/cl-format` by
  not requiring a writer to be specified when a formatted string is all that
  is needed."
  [format-in & args]
  (apply cl-format (conj args format-in nil)))

(defn non-nil!
  "Asserts (using `assert`) that `x` is not nil and returns it if it is not."
  ([x]
   (non-nil! x nil))
  ([x message]
   (assert (not (nil? x)) message)
   x))

(defn single!
  "Returns the only element of a collection, and throws an exception if there
  is not exactly one element in the sequence."
  [coll]
  (assert (not (empty? coll)) "Expected a collection with a single element, found an empty collection.")
  (assert (empty? (rest coll)) "Expected a collection with a single element, found a collection with multiple elements.")
  (first coll))
