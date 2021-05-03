(ns demo.commons.intrinio.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::securities
  (fn [db _]
      (:securities db)))

(reg-sub
  ::indices
  (fn [db _]
      (:indices db)))