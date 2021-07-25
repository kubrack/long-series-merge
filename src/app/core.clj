(ns app.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

; since the spec guarantees left-zero-padding for date, 
; we'll drop "-" and compare these as num
(def biggest_date 100000000) ; 4+2+2 decimal places +1 more

(defn get-next [file]
  (let [rdr (io/reader file)]
    (fn [] 
      (first (line-seq rdr)))))

(defn split-record [rec] (str/split rec #":"))

(defn parse-record [rec]
  (map #(Integer/parseInt %) 
       (split-record
         (str/replace-first rec #"(\d{4})-(\d{2})-(\d{2})" "$1$2$3"))))

(defn usage [] 
  (println "Usage: java -jar app-1.0-standalone.jar file1 [file2 ...] > outfile"))

(defn -main 
  ([] (usage))
  ([& files]
   (let [readers (map #(agent {:val (%) :fn %}) (map get-next files))]
     (while (some #(:val @%) readers)
       (let [min_date_reader (apply min-key #(first (parse-record (:val @%))) (filter #(-> @% :val nil? not) readers))
             min_date (first (parse-record (:val @min_date_reader)))
             min_date_txt (first (split-record (:val @min_date_reader)))
             min_date_readers (filter #(== min_date (first (parse-record (:val @%)))) (filter #(-> @% :val nil? not) readers))]
         (println (format "%s:%s" min_date_txt (apply + (map #(second (parse-record (:val @%))) min_date_readers))))
         (apply await (map #(send-off % assoc :val ((:fn @%)) ) min_date_readers)))))))

