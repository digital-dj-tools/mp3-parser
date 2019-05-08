(ns mp3-parser.xing
  (:require
   [mp3-parser.id3v2 :as id3v2]
   [mp3-parser.mpeg :as mpeg]
   [octet.core :as o]))

; the xing tag spec
(def spec (o/spec :keyword (o/string 4)))

(def offsets
  {1 {1 17
      2 32}
   2 {1 9
      2 17}})

(defn parse
  [buf mpeg-parsed]
  (if (not (::mpeg/valid? mpeg-parsed))
    mpeg-parsed
    (do
      (assert (>= (o/get-capacity buf) (+
                                   (::id3v2/offset mpeg-parsed)
                                   (::mpeg/frame-length mpeg-parsed)))
              (str "Buffer size " (o/get-capacity buf) " insufficient for frame length " (::mpeg/frame-length mpeg-parsed)))
      (let [offset (+ (::id3v2/offset mpeg-parsed)
                      (count (::mpeg/header mpeg-parsed))
                      (get-in offsets [(::mpeg/version mpeg-parsed) (::mpeg/num-channels mpeg-parsed)]))
            parsed (o/read buf spec {:offset offset})
            tag? (if (or (= (:keyword parsed) "Xing") (= (:keyword parsed) "Info")) true false)
            xing-parsed (assoc mpeg-parsed ::tag? tag?)]
        (if (not tag?)
          xing-parsed
          (assoc xing-parsed
                 ::keyword (:keyword parsed)
                 ::offset offset))))))