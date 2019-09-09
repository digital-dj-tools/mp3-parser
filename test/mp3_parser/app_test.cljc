(ns mp3-parser.app-test
  (:require [mp3-parser.app :as app]
            #?(:clj [mp3-parser.fs :as fs])
            [mp3-parser.id3v2 :as id3v2]
            [mp3-parser.lame :as lame]
            [mp3-parser.xing :as xing]
            #?(:clj [clojure.edn :refer [read-string]] :cljs [cljs.reader :refer [read-string]])
            #?(:clj [clojure.java.io :as io] :cljs [cljs-node-io.core :as io :refer [slurp spit]])
            [clojure.test :refer [deftest are is]])
  #?(:cljs (:require-macros [mp3-parser.fs :as fs])))

(deftest parse-test
  (let [fixtures (read-string (slurp "test-resources/parse-fixtures.edn"))]
    (doseq [fixture fixtures]
      (fs/with-buffered [buf (:file fixture)
                         len (+ 65536 2881)]
        (let [parsed (app/parse buf)]
          (are [f p] (= f p)
            (:id3v2-offset fixture) (:id3v2-offset parsed)
            (:xing-tag? fixture) (:xing-tag? parsed)
            (:xing-keyword fixture) (get-in parsed [:xing ::xing/keyword])
            (:lame-tag? fixture) (:lame-tag? parsed)
            (:lame-encoder fixture) (get-in parsed [:lame ::lame/encoder])
            (:lame-crc-valid? fixture) (get-in parsed [:lame ::lame/crc-valid?])))))))