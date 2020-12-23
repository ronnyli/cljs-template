(ns demo.components.screener.utils)

(defn check-for-nil [m k]
  (->> m
       keys
       (reduce
         (fn [xs i]
             (conj xs (get-in m [i k])))
         [])
       (some nil?)
       not))

(defn nil-values? [m]
  (let [clauses (-> m :clauses)]
    (when (seq clauses) (check-for-nil clauses :value))))

(defn nil-fields? [m]
  (let [clauses (-> m :clauses)]
    (when (seq clauses) (check-for-nil clauses :field))))
