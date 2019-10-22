
(ns familiar.alpha.core
  "Highly experimental extensions to core Clojure.  This namespace may undergo
  dramatic interface changes, or may disappear entirely."
  (:refer-clojure :exclude [cond cond-> cond->> let]))

(defmacro cond
  "Like clojure.core/cond, but with wrapped bindings to facilitate
  indentation-driven inference of clause grouping; e.g.: the clojure.core/cond
  form

  (cond (nil? x)
      :foo
      (= \"bar\" x)
      :bar
      :else
      :baz)

  is written as

  (cond [(nil? x)
         :foo]
        [(= \"bar\" x)
         :bar]
        [:else
         :baz])

  which is advantageous as the expressions in the clauses increase in length."
  [& clauses]
  `(clojure.core/cond ~@(mapcat identity clauses)))

(defmacro cond->
  "Like clojure.core/cond->, but with wrapped bindings to facilitate
  indentation-driven inference of clause grouping; e.g.: the clojure.core/cond->
  form

  (cond->
      x
    (let [[y (:foo x)]] (and (int? y) (even? y)))
    (update :foo inc)
    (let [[y (:bar x)]] (and (int? y) (even? y)))
    (update :bar inc))

  is written as

  (cond->
      x
    [(let [[y (:foo x)]] (and (int? y) (even? y)))
     (update :foo inc)]
    [(let [[y (:bar x)]] (and (int? y) (even? y)))
     (update :bar inc)])

  which is advantageous as the expressions in the clauses increase in length."
  [expr & clauses]
  `(clojure.core/cond-> ~expr ~@(mapcat identity clauses)))

(defmacro cond->>
  "Like clojure.core/cond->>, but with wrapped bindings to facilitate
  indentation-driven inference of clause grouping; e.g.: the clojure.core/cond->>
  form

  (cond->>
      x
    (let [[y (:foo x)]] (and (int? y) (even? y)))
    (#(update % :foo inc))
    (let [[y (:bar x)]] (and (int? y) (even? y)))
    (#(update % :bar inc)))

  is written as

  (cond->
      x
    [(let [[y (:foo x)]] (and (int? y) (even? y)))
     (#(update % :foo inc))]
    [(let [[y (:bar x)]] (and (int? y) (even? y)))
     (#(update % :bar inc))])

  which is advantageous as the expressions in the clauses increase in length."
  [expr & clauses]
  `(clojure.core/cond->> ~expr ~@(mapcat identity clauses)))

(defmacro let
  "Like clojure.core/let, but with wrapped bindings to facilitate
  indentation-driven inference of binding grouping; e.g.: the clojure.core/let
  form

  (let [x :a
        y :b]
    [x y])

  is written as

  (let [[x :a]
        [y :b]]
    [x y])

  which is adantagous as the names and expressions in bindings increase in
  length."
  [bindings & body]
  `(clojure.core/let ~(into [] (mapcat identity bindings))
     ~@body))
