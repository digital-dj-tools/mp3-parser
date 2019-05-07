(ns mp3-parser.id3v2-test
  (:require [mp3-parser.id3v2 :as id3v2]
            [clojure.test :refer [deftest is]]))

(deftest unsynchsafe-test
  (is (= 255 (id3v2/unsynchsafe 383))))

(deftest kth-bit-set-test
  (is (true? (id3v2/is-kth-bit-set 5 1)))
  (is (false? (id3v2/is-kth-bit-set 2 3))))