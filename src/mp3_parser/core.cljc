(ns mp3-parser.core
  (:require 
   [mp3-parser.id3v2 :as id3v2]
   [mp3-parser.mpeg :as mpeg]
   [mp3-parser.xing :as xing]
   [octet.core :as o]
   #?(:clj [clojure.java.io :as io])
   #?(:clj [nio.core :as nio])
   #?(:cljs ["fs" :as fs])))

; xing true/false
; lame true/false
; lame crc-16

(defn parse-buf
  [buf]
  (->> buf
       id3v2/parse
       (mpeg/parse buf)
       (xing/parse buf)
       (into (sorted-map))))

; TODO async version w/core async
#?(:cljs
   (defn parse
     [f]
     (let [len (+ 65536 2881)
           buf (o/allocate len)
           fd (.openSync fs f "r")]
       (try
         (.readSync fs fd buf 0 len nil)
         (parse-buf buf)
         (finally
           (.closeSync fs fd))))))

; TODO async version w/core async
#?(:clj
   (defn parse
     [f]
     (let [len (+ 65536 2881)
           buf (o/allocate len)]
       (with-open [ch (nio/channel (io/file f))]
         ; TODO handle file doesn't exist, empty, invalid
         (.read ch buf)
         (parse-buf buf)))))

