(ns mp3-parser.core
  (:require 
   [mp3-parser.id3v2 :as id3v2]
   [octet.core :as o]
   #?(:clj [clojure.java.io :as io])
   #?(:clj [nio.core :as nio])
   #?(:cljs ["fs" :as fs])))

(defn parse-buf
  [buf]
  (let [maybe-id3v2 (o/read buf id3v2/spec)]
    {:id3v2-offset (id3v2/offset maybe-id3v2)}))

#?(:cljs
   (defn parse
     [f]
     (let [len 10
           buf (o/allocate len)
           fd (.openSync fs f "r")]
       (try
         (.readSync fs fd buf 0 len nil)
         (parse-buf buf)
         (finally
           (.closeSync fs fd))))))

#?(:clj
   (defn parse
     [f]
     (let [len 10
           buf (o/allocate len)]
       (with-open [ch (nio/channel (io/file f))]
         (.read ch buf)
         (parse-buf buf)))))

