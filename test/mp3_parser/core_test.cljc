(ns mp3-parser.core-test
  (:require [mp3-parser.core :as p]
            [mp3-parser.id3v2 :as id3v2]
            [mp3-parser.xing :as xing]
            #?(:clj [clojure.edn :refer [read-string]] :cljs [cljs.reader :refer [read-string]])
            #?(:clj [clojure.java.io :as io] :cljs [cljs-node-io.core :as io :refer [slurp spit]])
            [clojure.test :refer [deftest is]]))

(deftest parse-test
  (let [fixtures (read-string (slurp "test-resources/parse-fixtures.edn"))]
    (doseq [fixture fixtures]
      (let [parsed (p/parse (:file fixture))]
        (is (= (:id3v2-offset fixture) (::id3v2/offset parsed)))
        (is (= (:xing-tag? fixture) (::xing/tag? parsed)))))))