(ns spcr.core-test
  (:require [clojure.test :refer :all]
            [spcr.core :refer :all]))

(deftest a-test
  (testing "Checking test-data path"
    (is (> (.length test-data) (.length "PEB_ETF.csv")))))
