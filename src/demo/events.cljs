(ns demo.events
  (:require
    [re-frame.core :as rf]
    [schema.core :as s :include-macros true]
    [tupelo.core :as t]
    [tupelo.schema :as tsk]))

(def logical-operators
  [{:key "AND" :label "AND"} {:key "OR" :label "OR"} {:key "NOT" :label "NOT"}])

(def comparator-operators
  {:id "comparator-operators"
   :placeholder ">"
   :values [{:key "gt" :label ">"} {:key "gte" :label ">="} {:key "eq" :label "="} {:key "lte" :label "<="} {:key "lt" :label "<"}]}) ;{:key "contains" :label "contains"}]})

(def factors
  {:id "factors"
   :placeholder "Add screener factors"
   :values [{:key :numerical :label "Numerical Factors"} {:key :categorical :label "Categorical Factors"} {:key :groups :label "Grouped Factors"}]})

(def numerical-metrics
  {:id "numerical-metrics"
   :placeholder "Choose metric"
   :values [{:key "marketcap" :label "Market cap"} {:key "adj_volume" :label "Volume"} {:key "pricetoearnings" :label "P/E ratio"} {:key "pricetobook" :label "P/B ratio"}]})

(def categorical-factors
  {:id "categorical-factors"
   :placeholder "Categorical Factors"
   :values [{:key "stock_exchange" :label "Exchange"} {:key "sector" :label "Sector"} {:key "industry_category" :label "Industry"}]})

(def initial-screener {:operator nil :clauses {}})

(def db-schema
  {(s/optional-key :root)                 tsk/Map
   (s/optional-key :logical-operators)    [tsk/Map]
   (s/optional-key :comparator-operators) tsk/Map
   (s/optional-key :factors)              tsk/Map
   (s/optional-key :numerical-metrics)    tsk/Map
   (s/optional-key :categorical-factors)  tsk/Map
   (s/optional-key :screener)             tsk/Map
   (s/optional-key :securities)           (s/maybe [tsk/Map])
   (s/optional-key :indices)              (s/maybe [tsk/Map])
   :modal                                 {:content (s/maybe s/Str)
                                           :headers (s/maybe s/Str)
                                           :body    (s/maybe s/Str)}})

(s/defn initialize :- tsk/KeyMap
        [_ :- s/Any
         _ :- s/Any]
        {:logical-operators    logical-operators
         :comparator-operators comparator-operators
         :factors              factors
         :numerical-metrics    numerical-metrics
         :categorical-factors  categorical-factors
         :screener             initial-screener
         :securities           nil
         :indices              nil
         :modal                {:content nil
                                :headers nil
                                :body    nil}})

(rf/reg-event-db
  :initialize
  initialize)

(rf/reg-event-db
  :show-modal
  (fn [db [_ data]]
      (assoc-in db [:modal] data)))
      ;db))
