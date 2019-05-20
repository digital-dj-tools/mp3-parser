(ns mp3-parser.cli
  (:require
   #?(:cljs [cljs.nodejs :as nodejs])
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [mp3-parser.app :as app]
   [mp3-parser.error :as err]
   #?(:clj [mp3-parser.fs :as fs]))
  #?(:cljs (:require-macros [mp3-parser.fs :as fs]))
  #?(:clj (:gen-class)))

(def option-specs
  [["-h" "--help"]])

(defn usage-message
  [summary]
  (->> ["Usage: mp3-parser [options] <file>"
        ""
        "Options:"
        summary]
       (str/join \newline)))

(defn error-message
  [errors]
  (as-> ["The following errors occurred while parsing the command:"
         ""] $
    (concat $ errors)
    (str/join \newline $)))

(defn parse-args
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args option-specs)]
    (cond
      (:help options) {:exit-message (usage-message summary) :help? true}
      errors {:exit-message (error-message errors)}
      (= 1 (count arguments)) {:arguments {:file (first arguments)}
                               :options options}
      :else
      {:exit-message (usage-message summary)})))

; TODO move to utils/core
(defn println-err
  [& objs]
  #?(:clj (binding [*out* *err*]
            (apply println objs))
     :cljs (binding [*print-fn* *print-err-fn*]
             (apply println objs))))

(defn exit
  [status message]
  (if (= 0 status)
    (println message)
    (println-err message))
  #?(:clj (System/exit status)
     :cljs (.exit nodejs/process status)))

; TODO move to utils/json
(defn pprint
  [x]
  #?(:clj (clojure.data.json/pprint x)
     :cljs (.log js/console (js/JSON.stringify (clj->js x) nil 1))))

#?(:clj
   (defn process
     [arguments options]
     (try
       (fs/with-buffered [buf (:file arguments)
                          len (+ 524288 2881)]
         (-> buf
             app/parse
             pprint))
       [0 "Parsing completed"]
       (catch Throwable t (do
                            (err/write-report (err/create-report arguments options (Throwable->map t)))
                            [2 "Problems parsing, please provide error-report.edn file..."])))))

#?(:cljs
   (defn process
     [arguments options]
     (try
       (fs/with-buffered [buf (:file arguments)
                          len (+ 524288 2881)]
         (-> buf
             app/parse
             pprint))
       [0 "Parsing completed"]
       (catch :default e (do
                           (err/write-report (err/create-report arguments options (err/Error->map e)))
                           [2 "Problems parsing, please provide error-report.edn file..."])))))

(defn -main
  [& args]
  (let [{:keys [arguments options exit-message help?]} (parse-args args)]
    (if exit-message
      (exit (if help? 0 1) exit-message)
      (let [result (process arguments options)
            status (first result )
            message (second result)]
        (if (not= 0 status)
          (exit status message))))))

#?(:cljs (set! *main-cli-fn* -main))
