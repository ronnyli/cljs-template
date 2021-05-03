(ns demo.commons.intrinio.utils)

(defn extract-leaf [{:keys [operator field value] :as tree}]
  (let [adjusted-value (if (= (:key field) "marketcap") (* value 1000000000) value)]
    (if operator
      (assoc tree :operator (:key operator) :field (:key field) :value adjusted-value)
      (assoc tree :operator "gt" :field (:key field) :value adjusted-value))))


(defn generate-request-map [{:keys [operator clauses groups] :as tree}]
  (let [refactored-operator (if operator operator "AND")
        refactored-clauses (->> clauses vals (map extract-leaf) vec)
        refactored-groups (when groups (generate-request-map groups))]
    (if (nil? refactored-groups)
      (assoc tree :operator refactored-operator :clauses refactored-clauses)
      (assoc tree :operator refactored-operator :clauses refactored-clauses :groups refactored-groups))))

(defn clj->json [ds]
  (->> ds
       clj->js
       (.stringify js/JSON)))

(defn add-price-and-type-request [m]
  (update m :clauses #(conj % {:field "adj_close_price" :value 0 :operator "gt"})))
                              ;{:field "security_type" :value "Ordinary Shares" :operator "contains"})))
