(ns mp3-parser.app
  (:require 
   [mp3-parser.id3v2 :as id3v2]
   [mp3-parser.mpeg :as mpeg]
   [mp3-parser.lame :as lame]
   [mp3-parser.xing :as xing]))

(defn parse
  [buf]
  (->> buf
       id3v2/parse
       (mpeg/parse buf)
       (xing/parse buf)
       (lame/parse buf)
       (into (sorted-map))))


