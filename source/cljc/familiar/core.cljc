
(ns familiar.core
  #?(:clj (:refer-clojure :exclude [format]))
  (:require [clojure.pprint :refer [cl-format]]))

(defn format
  "Replaces `clojure.core/format` with `clojure.pprint/cl-format`.  Takes the
  same arguments as `cl-format`.
  See: https://clojure.github.io/clojure/clojure.pprint-api.html#clojure.pprint/cl-format"
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

(let [empty-msg "Expected a collection with a single element, found an empty collection"
      many-msg "Expected a collection with a single element, found a collection with multiple elements"]
  (defn single!
    "Asserts (using `assert`) that `coll` contains only a single element; does
  not realise lazy sequences.

  The multi-arity versions of the functions allow additional context to be
  added to the assertion message; in the arity 2 case `msg` is appended to
  the default assertion msg, while in the arity 3 case `empty-msg` and
  `many-msg` *replace* the default assertion message for collections with
  <1 and >1 elements respectively."
    ([coll]
     (single! coll nil))
    ([coll msg]
     (let [extended-msg (if-not msg "" (str "; " msg))]
       (single! coll (str empty-msg extended-msg) (str many-msg extended-msg))))
    ([coll empty-msg many-msg]
     (assert (not (empty? coll)) (str empty-msg ": "))
     (assert (empty? (rest coll)) (str many-msg ": "))
     (first coll))))
