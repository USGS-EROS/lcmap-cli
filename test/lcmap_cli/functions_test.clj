(ns lcmap-cli.functions-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.functions :refer :all]
            [org.httpkit.client :as http-kit]
            [org.httpkit.fake :refer [with-fake-http]]))


(deftest to-json-test

  (testing "(to-json Hashmap)"
    (is (= "{\"key\":\"value\"}" (to-json {:key "value"}))))

  (testing "(to-json Integer)"
    (is (= "1" (to-json 1))))

  (testing "(to-json Float)"
    (is (= "1.0" (to-json 1.0))))

  (testing "(to-json Boolean)"
    (is (= "true" (to-json true))))

  (testing "(to-json Vector)"
    (is (= "[1,2,3]" (to-json [1 2 3]))))

  (testing "(to-json List)"
    (is (= "[1,2,3]" (to-json '(1 2 3)))))

  (testing "(to-json Set)"
    (is (= "[1,3,2]" (to-json #{1 2 3}))))
  
  (testing "(to-json String)"
    (is (= "\"a-value\"" (to-json "a-value"))))

  (testing "(to-json Keyword)"
    (is (= "\"a-keyword\"" (to-json :a-keyword))))

  (testing "(to-json Rational)"
    (is (= "0.3333333333333333" (to-json (/ 1 3)))))
  
  (testing "(to-json Exception)"
    (is (thrown? com.fasterxml.jackson.core.JsonGenerationException
                 (to-json (new java.lang.Object))))))

(deftest stdout-test
  (testing "testing stdout"
    (is (= 1 (stdout 1)))
    (is (= "test" (stdout "test")))
    (is (true? (stdout true)))
    (is (false? (stdout false)))
    (is (= {:a 1} (stdout {:a 1})))))

(deftest stderr-test
  (testing "testing stderr"
    (is (= 1 (stderr 1)))
    (is (= "test" (stderr "test")))
    (is (true? (stderr true)))
    (is (false? (stderr false)))
    (is (= {:a 1} (stderr {:a 1})))))

(deftest output-test
  (testing "testing output"
    (is (= 1 (output 1)))
    (is (= "test" (output "test")))
    (is (true? (output true)))
    (is (false? (output false)))
    (is (= {:a 1} (output {:a 1})))
    (is (thrown? com.fasterxml.jackson.core.JsonGenerationException
                 (output (Exception. "exceptions aren't json encodable"))))))

(deftest trim-test

  (testing "(trim java.lang.String)"
    (is (= "asdf" (trim "asdf     ")))
    (is (= "as   df" (trim "as   df")))
    (is (= "asdf" (trim "  asdf"))))

  (testing "(trim not-a-string)"
    (is (= 1 (trim 1)))))

(deftest transform-matrix-test

  (testing "(transform-matrix Hashmap)"
    (let [gs {:rx 3 :ry 3 :sx 1 :sy 1 :tx 2 :ty 2}]
      (is (= [[3 0 2][0 3 2][0 0 1.0]]
             (transform-matrix gs))))))

(deftest point-matrix-test

  (testing "(point-matrix Hashmap)"
    (let [p {:x "1" :y "3"}]
      (is (= [[1] [3] [1]] (point-matrix p))))))

(deftest tile-to-projection-test

  (testing "(tile-to-projection Hashmap)"

    (let [g      {:rx 1.0 :ry -1.0 :sx 150000.0
                  :sy 150000.0 :tx 2565585.0 :ty 3314805.0}
          h24v07 (tile-to-projection {:h 24 :v 7 :grid g})
          h00v00 (tile-to-projection {:h 0  :v 0 :grid g})]

      (is (= (:x h24v07) 1034415.0))
      (is (= (:y h24v07) 2264805.0))
      (is (= (:x h00v00) -2565585.0))
      (is (= (:y h00v00) 3314805.0)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; grids, grid, snap & near testing is covered
;; by testing the http client.
;;
;; (deftest detect-grids
;;  (testing "(detect Hashmap)"
;;    (is (= 1 0))))
;;
;; (deftest detect-grid
;;  (testing "(detect Hashmap)"
;;    (is (= 1 0))))
;;
;; (deftest detect-snap
;;  (testing "(detect Hashmap)"
;;    (is (= 1 0))))
;;
;; (deftest detect-near
;;  (testing "(detect Hashmap)"
;;    (is (= 1 0))))
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest tile-grid-test

  (testing "(tile-grid Hashmap)"
    (with-fake-http ["http://fake/grid" "[{\"name\": \"tile\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 150000.0,
                                           \"sy\": 150000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0},
                                          {\"name\": \"chip\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 3000.0,
                                           \"sy\": 3000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0}]]"]

      (is (= (tile-grid {:grid "fake-http" :dataset "ard"})
             {:name "tile"
              :proj ""
              :rx 1.0
              :ry -1.0
              :sx 150000.0
              :sy 150000.0
              :tx 2565585.0
              :ty 3314805.0})))))

(deftest chip-grid-test

  (testing "(chip-grid Hashmap)"
    (with-fake-http ["http://fake/grid" "[{\"name\": \"tile\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 150000.0,
                                           \"sy\": 150000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0},
                                          {\"name\": \"chip\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 3000.0,
                                           \"sy\": 3000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0}]]"]

      (is (= (chip-grid {:grid "fake-http" :dataset "ard"})
             {:name "chip"
              :proj ""
              :rx 1.0
              :ry -1.0
              :sx 3000.0
              :sy 3000.0
              :tx 2565585.0
              :ty 3314805.0})))))

(deftest lstrip0-test

  (testing "(lstrip0 java.lang.String)"
    (is (= (lstrip0 "00007") "7"))
    (is (= (lstrip0 "70000") "70000"))
    (is (= (lstrip0 "00 007") " 007"))
    (is (= (lstrip0 "7") "7"))))

(deftest string-to-tile-test

  (testing "(string-to-tile java.lang.String)"
    (is (= {:h 0 :v 0} (string-to-tile "000000")))
    (is (= {:h 4 :v 5} (string-to-tile "004005")))
    (is (= {:h 12 :v 7} (string-to-tile "012007")))))

(deftest tile-to-string-test

  (testing "(tile-to-string Integer Integer)"
    (is (= "000000" (tile-to-string 0 0)))
    (is (= "005007" (tile-to-string 5 7)))
    (is (= "111222" (tile-to-string 111 222)))))

(deftest xy-to-tile-test

  (testing "(xy-to-tile Hashmap)"
    (with-fake-http ["http://fake/grid/snap" "{\"tile\": {\"grid-pt\": [1,2]}}"]
      (is (= (xy-to-tile {:grid "fake-http" :dataset "ard" :x 123 :y 456})
             "001002")))))

(deftest tile-to-xy-test

  (testing "(tile-to-xy Hashmap)"
    (with-fake-http ["http://fake/grid" "[{\"name\": \"tile\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 150000.0,
                                           \"sy\": 150000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0},
                                          {\"name\": \"chip\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 3000.0,
                                           \"sy\": 3000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0}]]"]
      (is (= (tile-to-xy {:grid "fake-http" :dataset "ard" :tile "001001"})
             {:x -2415585.0 :y 3164805.0})))))

(deftest chips-test
  (testing "(chips Hashmap)"
    (with-fake-http ["http://fake/grid" "[{\"name\": \"tile\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 10.0,
                                           \"sy\": 10.0,
                                           \"tx\": 0.0,
                                           \"ty\": 0.0},
                                          {\"name\": \"chip\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 5.0,
                                           \"sy\": 5.0,
                                           \"tx\": 0.0,
                                           \"ty\": 0.0}]]"]
      (is (= (into #{} (chips {:grid "fake-http" :dataset "ard" :tile "001001"}))
             (into #{} [{:cx 10.0 :cy -10.0}
                        {:cx 15.0 :cy -10.0}
                        {:cx 10.0 :cy -15.0}
                        {:cx 15.0 :cy -15.0}]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; detect is tested as a result of testing
;; the http client
;;
;; (deftest detect-test
;;  (testing "(detect Hashmap)"
;;    (is (= 1 0))))
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



    
