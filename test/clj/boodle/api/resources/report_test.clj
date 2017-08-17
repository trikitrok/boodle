(ns boodle.api.resources.report-test
  (:require
   [boodle.model.report :as model]
   [boodle.api.resources.report :as r]
   [clojure.test :refer :all]))

(deftest test-get-date
  (testing "Testing get data resource"
    (with-redefs [model/get-data (fn [from to categories]
                                   [{:item "test" :amount 3.50}])]
      (is (= (r/get-data {:from "" :to "" :categories []})
             {:data [{:item "test" :amount "3,5"}] :total "3,5"})))))
