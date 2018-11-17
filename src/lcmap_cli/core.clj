(ns lcmap-cli.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http])
  (:gen-class :main true))


(comment 
(defn grid
  [grid src]
  (-> @(client :get grid src :grid nil)
      decode
      :body))

(defn snap
  [grid src x y]
  (-> @(client :get grid src :snap {:query-params {:x x :y y}})
      decode
      :body))

(defn near
  [grid src x y]
  (-> @(client :get grid src :near {:query-params {:x x :y y}})
      decode
      :body))

(defn tile
  ([grid src tile] nil)
  ([grid src x y] nil))

(defn chips
  [grid tile]
  nil)

(defn ingest
  [grid layer]
  nil)

(defn detect
  ([grid src x y]
   (-> @(client :post grid :ccdc :segment {:query-params {:cx x :cy y}})
       decode
       :body))
  ([grid tile]
   "Find all chips in tile, run them concurrently based on config"
   (chips grid tile)
   nil))
  
(defn train
  [grid tile]
  nil)

(defn predict
  ([grid x y] nil)
  ([grid tile] nil)))



(defn grid [_] (keys cfg/grids))

(defn grid-show
  [{:keys [grid dataset]}]
  (-> @(http/client :get (keyword grid) (keyword dataset) :grid nil)
      (http/decode)
      :body))

(defn grid-snap [{:keys [grid dataset x y]}]
  (-> @(http/client :get
                    (keyword grid)
                    (keyword dataset)
                    :snap
                    {:query-params {:x x :y y}})
      (http/decode)
      :body))

(defn grid-near [] nil)

(defn tile-lookup [] nil)

(defn tile-chips [] nil)

(defn ingest [] nil)

(defn ingest-list-available [] nil)

(defn ingest-list-completed [] nil)

(defn detect [] nil)

(defn detect-list-available [] nil)

(defn detect-list-completed [] nil)

(defn train [] nil)

(defn train-list-available [] nil)

(defn train-list-completed [] nil)

(defn predict [] nil)

(defn predict-list-available [] nil)

(defn predict-list-completed [] nil)

(defn product-maps [] nil)

(defn validate-args
  [args]
  args)

(defn exit [status msg]
  (println msg)
  (System/exit status))

(comment https://github.com/clojure/tools.cli#example-usage)

(defn options
  [keys]
  (let [o {:help ["-h" "--help"]
           :verbose ["-v" "--verbose"]
           :grid ["-g" "--grid GRID" "grid id"]
           :dataset ["-d" "--dataset DATASET" "dataset id"]
           :x ["-x" "--x X" "projection x coordinate"]
           :y ["-y" "--y Y" "projection y coordinate"]
           :tile ["-t" "--tile TILE" "tile id"]
           :source ["-f" "--source"]
           :start ["-s" "--start"]
           :end ["-e" "--end"]}]
    (vals (select-keys o keys))))

(def cli-options
  {:all [(options [:help :verbose])]
   :grid []
   :grid-show (into [] (options [:grid :dataset]))
   :grid-snap (into [] (options [:grid :dataset :x :y]))
   :grid-near [(options [:grid :x :y])]
   :tile-lookup [(options [:grid :tile :x :y])]
   :tile-chips [(options [:grid :tile])]
   :ingest [(options [:grid :source])]
   :ingest-list-available [(options [:grid :start :end])]
   :ingest-list-completed [(options [:grid :start :end])]
   :detect [(options [:grid :tile])]
   :detect-list-available [(options [:grid])]
   :detect-list-completed [(options [:grid])]
   :train [(options [:grid :tile])]
   :train-list-available [(options [:grid])]
   :train-list-completed [(options [:grid])]
   :predict [(options [:grid :tile])]
   :predict-list-available [(options [:grid])]
   :predict-list-completed [(options [:grid])]
   :product-maps [(options [:grid])]
   })

(defn usage
  [x]
  (str "command not found: " x))


(defn -main [& args]  
  (let [{arguments :arguments} (parse-opts args [])
        target (first arguments)
        func   (or (->> target (symbol "lcmap-cli.core") resolve) usage)
        opts   ((keyword target) cli-options)
        {aa :args oo :options ee :errors ss :summary} (parse-opts args opts)
        clean-opts (reduce-kv (fn [m k v] (assoc m k (string/trim v))) {} oo)]

    
    (comment
      (println "target:" target)
    (println "opts: " opts)
    (println "aa: " aa)
    (println "oo: " oo)
    (println "ee: " ee)
    (println "ss: " ss)
    (println "func: " func)
    (println "clean: " clean-opts))
    
    (try
      (println (func clean-opts))
      (catch  Exception e
        (println (str "caught exception: " e))))))

