
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
