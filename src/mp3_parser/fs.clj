(ns mp3-parser.fs
  (:require
   [clojure.java.io :as io]
   [net.cgrand.macrovich :as m]
   [nio.core :as nio]
   [octet.core :as o]))

; TODO async version w/core async
(defmacro with-buffered
  "bindings => [buf-name file len-name len]"
  [bindings & body]
  (let [buf-name (first bindings)
        f (second bindings)
        len-name (get bindings 2)
        length (get bindings 3)]
    `(m/case
      :clj
       (let [~len-name ~length
             ~buf-name (o/allocate ~len-name)]
         (with-open [ch# (nio/channel (io/file ~f))]
         ; TODO handle file doesn't exist, empty, invalid
           (.read ch# ~buf-name)
           ~@body))
       :cljs
       (let [~len-name ~length
             ~buf-name (o/allocate ~len-name)
             fs# (js/require "fs")
             fd# (.openSync fs# ~f "r")]
         (try
           (.readSync fs# fd# ~buf-name 0 ~len-name nil)
           ~@body
           (finally
             (.closeSync fs# fd#)))))))