(ns mp3-parser.lame
  (:require
   [mp3-parser.mpeg :as mpeg]
   [mp3-parser.xing :as xing]
   [octet.core :as o]))

; the lame tag spec
(def spec (o/spec :encoder (o/string 9)
                  :rev-method o/byte
                  :lowpass o/byte
                  :replay-gain (o/bytes 8)
                  :athtype-flags o/byte
                  :abr-bitrate o/byte
                  :delays (o/bytes 3)
                  :misc o/byte
                  :mp3-gain o/byte
                  :preset (o/bytes 2)
                  :music-length (o/bytes 4)
                  :music-crc (o/bytes 2)
                  :tag-crc (o/bytes 2)))

(defn revision
  [rev-method]
  (bit-shift-right rev-method 4))

(defn parse
  [buf {{::mpeg/keys [frame-length]} :mpeg {::xing/keys [offset]} :xing :as xing-parsed}]
  (if (not (:xing-tag? xing-parsed))
    xing-parsed
    (do
      (assert (>= (o/get-capacity buf) frame-length)
              (str "Buffer size " (o/get-capacity buf) " insufficient for frame length " frame-length))
      (let [parsed (o/read buf spec {:offset (+ offset 120)})
            revision (revision (:rev-method parsed))
            tag? (if (and (re-matches #"LAME.*" (:encoder parsed)) 
                          (or (= revision 0) (= revision 1) (= revision 15))) true false)
            lame-parsed (assoc xing-parsed :lame-tag? tag?)]
        (if (not tag?)
          lame-parsed
          (assoc lame-parsed :lame
                 (assoc (:lame lame-parsed)
                        ::encoder (:encoder parsed)
                        ::revision revision)))))))