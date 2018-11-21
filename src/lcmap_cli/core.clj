(ns lcmap-cli.core
  (:require [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap.commons.numbers :refer :all])
  (:gen-class :main true))

(comment 
(defn detect
  ([grid src x y]
   (-> @(client :post grid :ccdc :segment {:query-params {:cx x :cy y}})
       decode
       :body))
  ([grid tile]
   "Find all chips in tile, run them concurrently based on config"
   (chips grid tile)
   nil)))
-
(defn grids
  ([] (json/encode (map name (keys cfg/grids))))
  ([_](grids)))

(defn grid
  [{:keys [grid dataset]}]
  (:body @(http/client :get (keyword grid) (keyword dataset) :grid nil)))

(defn snap
  [{:keys [grid dataset x y]}]
  (:body @(http/client :get
                       (keyword grid)
                       (keyword dataset)
                       :snap
                       {:query-params {:x x :y y}})))

(defn near
  [{:keys [grid dataset x y]}]
  (:body @(http/client :get
                       (keyword grid)
                       (keyword dataset)
                       :near
                       {:query-params {:x x :y y}})))

(defn tile-grid
  [{g :grid d :dataset :as all}]
  (->> all
       grid
       json/decode
       keywordize-keys
       (filter #(= "tile" (:name %)))
       first))

(defn lstrip0
  [t]
  (loop [t t]
    (if (= "0" (str (first t)))
      (recur (rest t))
      (string/join t))))

(comment Create spec for tile id string... length 6, containing nums only)
(defn string->tile
  [tile-id]
  {:h (lstrip0 (subs tile-id 0 3))
   :v (lstrip0 (subs tile-id 3))})

(defn tile->string
  [h v]
  (format "%03d%03d" h v))


(s/def ::x #(numberize %))
(s/def ::y #(numberize %))
(s/def ::tile (s/and string? #(re-matches #"\d{6}" %)))
(s/def ::grid (s/and string? (fn [x] (some #(= x %) (->> (grids) json/decode)))))
(s/def ::dataset string?)
(s/def :tile/dispatch (s/or :xy   (s/keys :req-un [::grid ::dataset ::x ::y])
                            :tile (s/keys :req-un [::grid ::dataset ::tile])))                        


(defn xy-to-tile
  [{g :grid d :dataset x :x y :y :as all}]
  (let [{:keys [:rx :ry :tx :ty :sx :sy]} (tile-grid all)]
    :tile))

(defn tile-to-xy
  [{g :grid d :dataset t :tile :as all}]
  (let [{:keys [:rx :ry :tx :ty :sx :sy]} (tile-grid all)
        {:keys [:h :v]} (string->tile t)]
    :xy))


(defn chips [] nil)
(defn ingest [] nil)
(defn ingest-list-available [] nil)
(defn ingest-list-completed [] nil)
(defn detect [] nil)
(defn train [] nil)
(defn predict [] nil)
(defn product-maps [] nil)

(comment https://github.com/clojure/tools.cli#example-usage)

(defn options
  [keys]
  (let [o {:help ["-h" "--help"]
           :verbose ["-v" "--verbose"]
           :grid ["-g" "--grid GRID" "grid id"]
           :dataset ["-d" "--dataset DATASET" "dataset id"]
           :x ["-x" "--x X" "projection x coordinate" ] :parse-fn numberize
           :y ["-y" "--y Y" "projection y coordinate" :parse-fn numberize]
           :tile ["-t" "--tile TILE" "tile id"]
           :source ["-f" "--source"]
           :start ["-s" "--start"]
           :end ["-e" "--end"]}]
    (vals (select-keys o keys))))

(def cli-options
  {:grids      (into [] (options [:help]))
   :grid       (into [] (options [:help :grid :dataset]))
   :snap       (into [] (options [:help :grid :dataset :x :y]))
   :near       (into [] (options [:help :grid :dataset :x :y]))
   :xy-to-tile (into [] (options [:help :grid :dataset :x :y]))
   :tile-to-xy (into [] (options [:help :grid :dataset :tile]))
   :chips      (into [] (options [:help :grid :tile]))
   :ingest     (into [] (options [:help :grid :source]))
   :ingest-list-available (into [] (options [:help :grid :start :end]))
   :ingest-list-completed (into [] (options [:help :grid :start :end]))
   :detect  (into [] (options [:help :grid :tile]))
   :train   (into [] (options [:help :grid :tile]))
   :predict (into [] (options [:help :grid :tile]))
   :product-maps (into [] (options [:help :grid]))
   })

 (defn usage [action options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        (str "Usage: lcmap " action " [options]" )
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(def actions
  (str "Available actions: " (keys cli-options)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn function
  [args]
  (->> args str (symbol "lcmap-cli.core") resolve))

(defn ->trim
  [v]
  (if (string? v)
    (string/trim v)
    v))

0(defn parameters
  [args]
  (let [p (parse-opts args (-> args first keyword cli-options))]
        (assoc p :options (reduce-kv (fn [m k v] (assoc m k (->trim v)))
                                     {}
                                     (:options p)))))

(comment 
(defn -main [& args]

  (let [func (or (-> args first function) nil)
        parm (-> args parameters)
        _ (println parm)]

    (if (or (:errors parm) (nil? func))
      (println (:summary parm))
      (try (println (func (:options parm)))
           (catch  Exception e
             (binding [*out* *err*](println "caught exception: " e)))))))
)


(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args (-> args first keyword cli-options))]
    
    (cond
      (:help options)
      {:exit-message (usage (-> args first) summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (and (= 1 arguments) (into #{} (keys cli-options)) (-> arguments first keyword))
      {:action (first arguments) :options options}

      :else
      {:exit-message (usage (-> args first) summary)})))


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (println action)
    (println options)
    (println exit-message)
    (println ok?)
    (println "===")
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (println ((function action) options))
        (catch Exception e
          (binding [*out* *err*]
            (println "caught exception: " e)))))))
