(ns demo.components.screener.events
  (:require
   [re-frame.core :as rf]
   ;;
   [demo.commons.intrinio.events :as intrinio.events]))

(rf/reg-event-db
  ::add-metric
  (fn [db [_ path index]]
      (if (seq path)
        (assoc-in db (flatten [:screener path :clauses index]) {})
        (assoc-in db [:screener :clauses index] {}))))

(rf/reg-event-db
  :add-groups
  (fn [db [_ path]]
      (if (seq path)
        (assoc-in db (flatten [:screener path :groups]) {:operator nil :clauses {}})
        (assoc-in db [:screener :groups] {:operator nil :clauses {}}))))

(rf/reg-event-db
  :set-logical-operator
  (fn [db [_ path value]]
      (assoc-in db (flatten [:screener path :operator]) value)))

(rf/reg-event-fx
  ::set-numerical-metric
  (fn [{db :db} [_ path index value]]
    {:dispatch-n [[::intrinio.events/reset-securities]
                  [::intrinio.events/screen-securities]]
     :db (if (seq path)
           (assoc-in db (flatten [:screener path :clauses index :field]) value)
           (assoc-in db [:screener :clauses index :field] value))}))

(rf/reg-event-fx
  ::set-comparator-operator
  (fn [{db :db} [_ path index value]]
    {:dispatch-n [[::intrinio.events/reset-securities]
                  [::intrinio.events/screen-securities]]
     :db (if (seq path)
           (assoc-in db (flatten [:screener path :clauses index :operator]) value)
           (assoc-in db [:screener :clauses index :operator] value))}))

(rf/reg-event-fx
  ::set-value
  (fn [{db :db} [_ path index value]]
    {:dispatch-n [[::intrinio.events/reset-securities]
                  [::intrinio.events/screen-securities]]
     :db (if (seq path)
           (assoc-in db (flatten [:screener path :clauses index :value]) value)
           (assoc-in db [:screener :clauses index :value] value))}))

(rf/reg-event-db
  ::reset-screener
  (fn [db _]
    (assoc db :screener {:operator nil :clauses {}})))
