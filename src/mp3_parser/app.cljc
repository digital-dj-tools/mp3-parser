(ns mp3-parser.app
  (:require
   #?(:clj [clojure.java.io :as io])
   #?(:cljs ["fs" :as node-fs])
   #?(:clj [mp3-parser.fs :as fs])
   [mp3-parser.id3v2 :as id3v2]
   [mp3-parser.lame :as lame]
   [mp3-parser.mpeg :as mpeg]
   [mp3-parser.xing :as xing]
   #?(:clj [nio.core :as nio])
   [octet.core :as o])
  #?(:cljs (:require-macros [mp3-parser.fs :as fs])))

(def id3v2-header-size 10)

(def max-frame-size 2881)

(defn parse
  [fname]
  (fs/with-open-sync [f #?(:clj (nio/channel (io/file fname))
                           :cljs (.openSync node-fs fname "r"))]
    (let [id3v2-buf (o/allocate id3v2-header-size)]
      (do
        #?(:clj (.read f id3v2-buf)
           :cljs (.readSync node-fs f id3v2-buf 0 id3v2-header-size nil))
        (let [id3v2-parsed (id3v2/parse id3v2-buf)
              id3v2-offset (:id3v2-offset id3v2-parsed)
              buf (o/allocate max-frame-size)]
          (do
            #?(:clj (.read f buf id3v2-offset)
               :cljs (.readSync node-fs f buf 0 max-frame-size id3v2-offset))
            (->> id3v2-parsed
                 (mpeg/parse buf)
                 (xing/parse buf)
                 (lame/parse buf)
                 (into (sorted-map)))))))))