(require
 '[mp3-parser.app :as app])

(def mp3-file
  "test-resources/lame-mp3-cbr-stereo-44100khz-192kbps-id3v2.mp3")

(app/parse mp3-file)
