(ns mp3-parser.id3v2
  (:require [octet.core :as o]))

(def spec (o/spec :id3v2-begin (o/string 3)
                  :byte-3 o/byte
                  :byte-4 o/byte
                  :byte-5 o/byte
                  :synchsafe o/int32))

; https://en.wikipedia.org/wiki/Synchsafe
(defn unsynchsafe
  [ss]
  (loop [out 0 mask 0x7F000000]
    (if (> mask 0) ; TODO use pos?
      (recur (bit-or (bit-shift-right out 1) (bit-and ss mask)) (bit-shift-right mask 8))
      out)))

(defn is-kth-bit-set
  [n k]
  (if (> (bit-and n (bit-shift-left 1 (- k 1))) 0) ; TODO use pos?
    true
    false))

; http://fileformats.archiveteam.org/wiki/ID3#How_to_skip_past_an_ID3v2_segment
(defn offset
  [maybe-id3v2]
  (if (= "ID3" (:id3v2-begin maybe-id3v2))
    (let [id3v2-header-length 10
          unsynchsafe (unsynchsafe (:synchsafe maybe-id3v2))
          footer 10]
      (if (is-kth-bit-set (:byte-5 maybe-id3v2) 2)
        (+ id3v2-header-length unsynchsafe footer)
        (+ id3v2-header-length unsynchsafe)))
    0))

(defn parse
  [buf]
  (let [parsed (o/read buf spec)]
    {::offset (offset parsed)}))