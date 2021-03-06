#?(:clj (set! *warn-on-reflection* true))
(ns mp3-parser.xing
  (:require
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
  [buf {{::mpeg/keys [header version num-channels frame-length]} :mpeg :as mpeg-parsed}]
  (if (not (:mpeg-valid? mpeg-parsed))
    mpeg-parsed
    (do
      (assert (>= (o/get-capacity buf) frame-length)
              (str "Buffer size " (o/get-capacity buf) " insufficient for frame length " frame-length))
      (let [offset (+ (count header)
                      (get-in offsets [version num-channels]))
            parsed (o/read buf spec {:offset offset})
            tag? (if (or (= (:keyword parsed) "Xing") (= (:keyword parsed) "Info")) true false)
            xing-parsed (assoc mpeg-parsed :xing-tag? tag?)]
        (if (not tag?)
          xing-parsed
          (assoc xing-parsed :xing
                 (assoc (:xing xing-parsed)
                        ::keyword (:keyword parsed)
                        ::offset offset)))))))