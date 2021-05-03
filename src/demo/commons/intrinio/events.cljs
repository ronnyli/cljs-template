(ns demo.commons.intrinio.events
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.http-fx]
    [ajax.core :as ajax]
    ;;
    [demo.commons.intrinio.utils :as intrinio.utils]
    [demo.components.screener.utils :as screener.utils]
    [demo.commons.intrinio.indices.spx :as indices.spx]
    [demo.commons.intrinio.indices.dj :as indices.dj]
    [demo.commons.intrinio.indices.nasdaq :as indices.nasdaq]))

(rf/reg-event-fx
  :success-screen-securities
  (fn [{db :db} [_ securities]]
      (let [extract-fn (fn [{:keys [security data]}]
                           (-> security
                               (select-keys [:id :name :ticker :currency :last-price :type])
                               (assoc :last-price (->> data
                                                       (filter #(= (:tag %) "adj_close_price"))
                                                       first
                                                       :number_value))
                               (assoc :type (->> data
                                                 (filter #(= (:tag %) "security_type"))
                                                 first
                                                 :text_value))))
            ;    tickers (map #(-> % :security :ticker) securities)
            new-db (->> securities
                        (map extract-fn)
                        (assoc db :securities))]
             ;(if (seq securities)
             ;  {:dispatch-n (map (fn [ticker] [:request-ticker-details ticker]) tickers)
           {:db new-db})))
           ;{:db db})))

(rf/reg-event-db
  :failure-screen-securities
  (fn [db [_ request-error]]
    (.log js/console (str "failure screen securities" request-error))
    db))

(rf/reg-event-fx
  ::screen-securities
  (fn [{db :db} _]
      (let [screener (:screener db)
            values (screener.utils/nil-values? screener)
            fields (screener.utils/nil-fields? screener)]
        (when (and values fields)
          {:http-xhrio {:method          :post
                        :uri             "https://api-v2.intrinio.com/securities/screen?page_size=10000"
                        :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                        :timeout         25000
                        :body            (-> screener
                                             intrinio.utils/generate-request-map
                                             intrinio.utils/add-price-and-type-request
                                             intrinio.utils/clj->json)
                        :format          (ajax/json-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [:success-screen-securities]
                        :on-failure      [:failure-screen-securities]}}))))
          ;{:dispatch [::reset-securities]}))))

(rf/reg-event-fx
  :success-ticker-details
  (fn [{db :db} [_ {:keys [id ticker type currency] :as security}]]
    (let [securities (-> db :securities)
          extract-fn #(select-keys % [:id :name :ticker :currency :type])]
      (if (nil? securities)
        {:dispatch [:request-ticker-last-price ticker]
         :db (assoc db :securities [(extract-fn security)])}

        {:dispatch [:request-ticker-last-price ticker]
         :db (let [securities (-> db :securities)
                   index (->> securities
                              (map-indexed vector)
                              (filter (fn [[i v]] (or (= (:id v) id)
                                                      (= (:ticker v) ticker))))
                              first
                              first)
                   not-ordinary-share? (-> type (.includes "Ordinary Shares") not)
                   not-usd? (-> currency (.includes "USD") not)]
               (cond
                 (or not-ordinary-share? not-usd?)
                 (assoc db :securities (filter #(not= (:id %) id) securities))

                 (nil? index)
                 (update db :securities #(conj % (extract-fn security)))

                 index
                 (assoc db :securities
                           (-> securities
                               vec
                               (update-in [index] #(assoc % :type type :currency currency))))

                 :else db))}))))

(rf/reg-event-db
  :failure-ticker-details
  (fn [db [_ request-error]]
      (.log js/console (str "add ticker details" request-error))
      db))

(rf/reg-event-fx
  :request-ticker-details
  (fn [_ [_ ticker]]
    {:http-xhrio {:method          :get
                  :uri             (str "https://api-v2.intrinio.com/securities/" ticker)
                  :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                  :timeout         25000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:success-ticker-details]
                  :on-failure      [:failure-ticker-details]}}))

(defn update-values-in-db [db ticker key value]
  (let [securities (-> db :securities)
        index (->> securities
                   (map-indexed vector)
                   (filter (fn [[_ v]] (= (:ticker v) ticker)))
                   first
                   first)]
       (cond
         index
         (assoc db :securities
                (-> securities
                    vec
                    (update-in [index] #(assoc % key value))))

         :else db)))

(rf/reg-event-db
  :success-ticker-last-price
  (fn [db [_ ticker last-price]]
      (update-values-in-db db ticker :last-price last-price)))

(rf/reg-event-db
  :success-ticker-security-type
  (fn [db [_ ticker security-type]]
      (update-values-in-db db ticker :type security-type)))

(rf/reg-event-db
  :failure-ticker-last-price
  (fn [db [_ request-error]]
      (.log js/console (str  "failure ticker prices" request-error))
      db))

(rf/reg-event-db
  :failure-ticker-security-type
  (fn [db [_ request-error]]
      (.log js/console (str  "failure ticker security type" request-error))
      db))

(rf/reg-event-fx
  :request-ticker-last-price
  (fn [_ [_ ticker]]
    {:http-xhrio {:method          :get
                  :uri             (str "https://api-v2.intrinio.com/securities/" ticker "/data_point/adj_close_price/number")
                  :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                  :params          {:page_size 1}
                  :timeout         25000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:success-ticker-last-price ticker]
                  :on-failure      [:failure-ticker-last-price]}}))

(rf/reg-event-fx
  :request-ticker-security-type
  (fn [_ [_ ticker]]
      {:http-xhrio {:method          :get
                    :uri             (str "https://api-v2.intrinio.com/securities/" ticker "/data_point/security_type/text")
                    :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                    :params          {:page_size 1}
                    :timeout         25000
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:success-ticker-security-type ticker]
                    :on-failure      [:failure-ticker-security-type]}}))

(rf/reg-event-fx
  :success-search-securities
  (fn [{db :db} [_ {:keys [securities]}]]
    (let [tickers (map :ticker securities)]
      (if (seq securities)
        {:dispatch-n (reduce
                       (fn [xs ticker]
                         (vec
                           (conj xs [:request-ticker-last-price ticker]
                                    [:request-ticker-security-type ticker])))
                       []
                       tickers)
         :db (->> securities
                  (map #(select-keys % [:id :name :ticker :currency :last-price :type]))
                  (filter #(= (:currency %) "USD"))
                  (assoc db :securities))}
        {:db db}))))

(rf/reg-event-db
  :failure-search-securities
  (fn [db [_ request-error]]
      (.log js/console (str "failure search securities" request-error))
      db))

(rf/reg-event-fx
  ::search-securities
  (fn [_ [_ symbol]]
      {:http-xhrio {:method          :get
                    :uri             (str "https://api-v2.intrinio.com/securities/search?query=" symbol)
                    :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                    :timeout         25000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:success-search-securities]
                    :on-failure      [:failure-search-securities]}}))

(rf/reg-event-db
  ::remove-from-securities
  (fn [db [_ ticker]]
    (->> db
         :securities
         (filter #(not= (:ticker %) ticker))
         (assoc db :securities))))

(rf/reg-event-db
  ::reset-securities
  (fn [db [_]]
    (let [securities (:securities db)]
      (if (seq securities)
        (assoc db :securities nil)
        db))))

(rf/reg-event-db
  :success-indices
  (fn [db [_ {:keys [indices]}]]
    (->> indices
         (map (fn [{:keys [symbol name]}] {:key symbol :label name}))
         (assoc {:id "indices"
                 :placeholder "Choose index"}
                :values)
         (assoc db :indices))))

(rf/reg-event-db
  :failure-indices
  (fn [db [_ request-error]]
      (.log js/console (str "failure indices" request-error))
      db))

(rf/reg-event-fx
  ::indices
  (fn [_ [_ {:keys [key]}]]
    {:http-xhrio {:method          :get
                  :uri             "https://api-v2.intrinio.com/indices/stock_market"
                  :headers         {"public-Key" "b4480e046ab6d941231510fc98ef2361"}
                  :params          {:page_size 100}
                  :timeout         25000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:success-indices]
                  :on-failure      [:failure-indices]}}))

(rf/reg-event-fx
  ::set-index
  (fn [{db :db} [_ index]]
    (let [new-db (assoc-in db [:indices :value] index)
          tickers (->> (case (:key index)
                             "$SPX" indices.spx/index
                             "$DJI" indices.dj/industrial
                             "$DJA" indices.dj/composite
                             "$TRAN" indices.dj/transportation
                             "$UTIL" indices.dj/utility
                             "$NDX" indices.nasdaq/hundred
                             "$COMPQ" indices.nasdaq/composite)
                       (map :ticker))]
      {:dispatch-n (map (fn [ticker] [:request-ticker-details ticker]) tickers)
       :db new-db})))


(rf/reg-event-db
  ::reset-indices
  (fn [db [_]]
    (let [indices (:indices db)]
      (if (seq indices)
        (assoc db :indices nil)
        db))))

