(ns demo.components.screener.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :logical-operators
  (fn [db _]
      (:logical-operators db)))

(reg-sub
  ::comparator-operators
  (fn [db _]
      (:comparator-operators db)))

(reg-sub
  :factors
  (fn [db _]
      (:factors db)))

(reg-sub
  ::numerical-metrics
  (fn [db _]
      (:numerical-metrics db)))

(reg-sub
  :categorical-factors
  (fn [db _]
      (:categorical-factors db)))

(reg-sub
  ::screener
  (fn [db _]
      (:screener db)))
