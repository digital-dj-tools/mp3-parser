(ns mp3-parser.app2
  (:require
   #?(:cljs ["fs" :as fs])
   #?(:clj [mp3-parser.fs2 :as fs2])
   [mp3-parser.id3v2 :as id3v2]
   [mp3-parser.lame :as lame]
   [mp3-parser.mpeg :as mpeg]
   [mp3-parser.xing :as xing]
   [octet.core :as o])
  #?(:cljs (:require-macros [mp3-parser.fs2 :as fs2])))

#?(:cljs
   (defn parse
     [f]
     (fs2/with-open-sync [fd (.openSync fs f "r")]
       (let [id3v2-buf (o/allocate 10)]
         (do
           (.readSync fs fd id3v2-buf 0 10 nil)
           (let [id3v2-parsed (id3v2/parse id3v2-buf)
                 id3v2-offset (:id3v2-offset id3v2-parsed)
                 buf (o/allocate 2881)]
             (do
               (.readSync fs fd buf 0 2881 id3v2-offset)
               (->> id3v2-parsed
                    (mpeg/parse buf)
                    (xing/parse buf)
                    (lame/parse buf)
                    (into (sorted-map))))))))))