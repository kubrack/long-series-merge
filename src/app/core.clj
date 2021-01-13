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

(defn extract-record "from :rec field of state" [pair] (first pair))
(defn extract-reader "from :rec field of state" [pair] (second pair))

(defn min-rec 
  "to find current minimum date and keep it together 
  with its source reader and parsed values" 
  [state]
  (reduce (fn 
            ([] {:date biggest_date 
                 :date_text nil}) ; last record
            ([r1 r2] (if (> (:date r1) (:date r2)) r2 r1)))

          (map (fn [rec] 
                 {:date (-> rec extract-record parse-record first)
                  :val (-> rec extract-record parse-record second)
                  :date_text (-> rec extract-record split-record first) 
                  :reader (-> rec extract-reader)})
               (filter #(-> % extract-record nil? not)
                       (:recs state)))))

(defn put-result [min_struct]
  (if (:date_text min_struct)
    (println (format "%s:%s" (:date_text min_struct) (:val min_struct)))))

(defn process [state]
  (let [last_min (:last_min state)
        current_min (min-rec state)
        recs (map (fn [rec]
                 (let [reader_from_state (extract-reader rec)]
                   (if (= reader_from_state (:reader current_min))
                     (list (reader_from_state) reader_from_state)
                     rec)))
                (filter #(-> % extract-record nil? not)
                  (:recs state)))]
    (if (> (:date current_min) (:date last_min))
      (do
        (put-result last_min)
        {:last_min current_min :recs recs})
      ; else == since the new min can't be less than prev due to asc in files ordering
      {:last_min {:date (:date current_min)
                   :date_text (:date_text current_min)
                   :val (+ (:val last_min) (:val current_min))}
                   ; the rest fields are unused in this atom 
        :recs recs})))

(defn usage [] 
  (println "Usage: java -jar app-1.0-standalone.jar file1 [file2 ...] > outfile"))

(defn -main 
  ([] (usage))
  ([& files] 
   (let [state (atom {:recs (map #(list (%) %) (map get-next files))
                      ; the :recs field is pair of (record reader_from_which_it_gotten)
                      :last_min {:date 0
                                 :date_text nil
                                 :val 0}})]
     (while (first (:recs @state))
       (swap! state process)))))

