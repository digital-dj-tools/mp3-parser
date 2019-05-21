(ns mp3-parser.fs2
  (:require [net.cgrand.macrovich :as m]))

(defmacro with-open-sync
  "bindings => [name init]"
  [bindings & body]
  (let [name (first bindings)
        init (second bindings)]
    `(m/case
      :clj
       (with-open [~name ~init]
         ~@body)
       :cljs
       (let [~name ~init
             fs# (js/require "fs")]
         (try
           ~@body
           (finally
             (.closeSync fs# ~name)))))))