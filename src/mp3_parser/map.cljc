#?(:clj (set! *warn-on-reflection* true))
(ns mp3-parser.map)

(defn m-assoc-in
  [m & kvvp]
  (reduce #(apply assoc-in %1 %2) m (partition 2 kvvp)))