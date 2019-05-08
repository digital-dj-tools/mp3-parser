(ns mp3-parser.mpeg
  (:require
   [mp3-parser.id3v2 :as id3v2]
   [octet.core :as o]))

; the mpeg frame header spec
(def spec (o/spec o/byte
                  o/byte
                  o/byte
                  o/byte))

(defn valid?
  [header]
  (= 0xFFF0
     (bit-or (bit-shift-left (bit-and (header 0) 0xFF) 8)
             (bit-and (header 1) 0xF0))))

(defn version
  [header]
  (let [version-bits (bit-shift-right (header 1) 3)]
    (cond (= 0x03 (bit-and version-bits 0x03)) 1
          (= 0x02 (bit-and version-bits 0x02)) 2
          :else 0)))

(defn layer
  [header]
  (let [layer-bits (bit-shift-right (header 1) 1)]
    (cond (= 0x03 (bit-and layer-bits 0x03)) 1
          (= 0x02 (bit-and layer-bits 0x02)) 2
          (= 0x01 (bit-and layer-bits 0x01)) 3
          :else 0)))

(defn has-padding
  [{::keys [header] :as mpeg-parsed}]
  (assoc mpeg-parsed
         ::has-padding?
         (= (bit-shift-right (bit-and (header 2) 0x02) 1)
            0x01)))

(defn num-channels
  [{::keys [header] :as mpeg-parsed}]
  (let [channel-bits (bit-shift-right (header 3) 6)
        channels (if (= (bit-and channel-bits 0x03) 0x03)
                   1
                   2)]
    (assoc mpeg-parsed ::num-channels channels)))

(def bit-rates
  {1 {1 [0 32 64 96 128 160 192 224 256 288 320 352 384 416 448 0]
      2 [0 32 48 56 64 80 96 112 128 160 192 224 256 320 384 0]
      3 [0 32 40 48 56 64 80 96 112 128 160 192 224 256 320 0]
      4 [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]}
   2 {1 [0 32 48 56 64 80 96 112 128 144 160 176 192 224 256 0]
      2 [0 8 16 24 32 40 48 56 64 80 96 112 128 144 160 0]
      3 [0 8 16 24 32 40 48 56 64 80 96 112 128 144 160 0]
      4 [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]}})

(defn bit-rate
  [{::keys [header version layer] :as mpeg-parsed}]
  (assoc mpeg-parsed
         ::bit-rate
         (*
          (get-in bit-rates [version layer (bit-and (bit-shift-right (header 2) 4) 0x0F)])
          1000)))

(def sample-rates {1 [44100 48000 32000 0]
                   2 [22050 24000 16000 0]})

(defn sample-rate
  [{::keys [header version] :as mpeg-parsed}]
  (assoc mpeg-parsed
         ::sample-rate
         (get-in sample-rates [version (bit-and (bit-shift-right (header 2) 2) 0x03)])))

(def samples-per-frame
  {1 {0 0
      1 384
      2 1152
      3 1152}
   2 {0 0
      1 384
      2 1152
      3 576}})

(defn num-samples
  [{::keys [version layer] :as mpeg-parsed}]
  (assoc mpeg-parsed
         ::num-samples
         (get-in samples-per-frame [version layer])))

(defn frame-length
  [{::keys [has-padding? bit-rate sample-rate num-samples frame-length] :as mpeg-parsed}]
  (let [padding (if has-padding? 1 0)
        length (if (= sample-rate 0)
                 0
                 (if (= layer 1)
                   (* (Math/floor (+ (* 12.0 (/ bit-rate sample-rate)) padding)) 4)
                   (+ (Math/floor (* (/ (/ bit-rate 8) sample-rate) num-samples)) padding)))]
    (assoc mpeg-parsed ::frame-length length)))

(defn parse
  [buf id3v2-parsed]
  (assert (>= (o/get-capacity buf) (+ (::id3v2/offset id3v2-parsed) 4))
          (str "Buffer size " (o/get-capacity buf) " insufficient for id3v2 offset " (::id3v2/offset id3v2-parsed) " and frame header length 4"))
  (let [header (o/read buf spec {:offset (::id3v2/offset id3v2-parsed)}
                       )
        valid? (valid? header)
        mpeg-parsed (assoc id3v2-parsed
                           ::valid? valid?
                           ::header header)]
    (if (not valid?)
      mpeg-parsed
      (let [version (version header)
            layer (layer header)
            mpeg-parsed-version-layer (assoc mpeg-parsed
                                             ::version version
                                             ::layer layer)]
        (if (and (pos? version) (pos? layer))
          (-> mpeg-parsed-version-layer
              has-padding
              num-channels
              bit-rate
              sample-rate
              num-samples
              frame-length)
          mpeg-parsed-version-layer)))))