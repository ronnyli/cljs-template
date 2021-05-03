(ns demo.components.screener.view
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ;;
    [demo.commons.icons :as icons]
    [demo.commons.select :as select]
    [demo.components.utils :as utils]
    [demo.components.screener.events :as screener.events]
    [demo.components.screener.subscriptions :as screener.subs]
    [demo.components.screener.utils :as screener.utils]
    [demo.commons.intrinio.events :as intrinio.events]
    [demo.commons.intrinio.subscriptions :as intrinio.subs]))

(defn build-numerical-clause [path index]
  (let [screener (rf/subscribe [::screener.subs/screener])
        metrics @(rf/subscribe [::screener.subs/numerical-metrics])
        operators @(rf/subscribe [::screener.subs/comparator-operators])
        {:keys [field operator value]} (get-in @screener (->> index (conj path :clauses) flatten))]
    [:div {:class "flex flex-row"}
     [select/view
      (assoc metrics :value field)
      #(rf/dispatch [::screener.events/set-numerical-metric path index %])]
     [select/view
      (assoc operators :value operator)
      #(rf/dispatch [::screener.events/set-comparator-operator path index %])]
     [:div
      [:form
       [:input {:id "numerical-input"
                :placeholder (if (= "marketcap" (:key field)) "Enter value (in billions)" "Enter value")
                :value value
                :on-change #(rf/dispatch [::screener.events/set-value path index (utils/target->integer %)])}]]]]))

(defn build-extended-clauses [path]
  (let [screener (rf/subscribe [:screener])
        logical-operators @(rf/subscribe [:logical-operators])
        factors @(rf/subscribe [:factors])
        {:keys [operator clauses groups]} (if (seq path) (get-in @screener path) @screener)
        indices (keys clauses)]
    [:div {:class "flex flex-col pl-10"}
     [:div {:class "flex flex-row"}
      (when (> (count indices) 1)
        (for [{:keys [key label]} logical-operators]
          ^{:key key}
          [:div
           [:button {:class (if (= operator key) "bg-blue-500" "bg-gray-100")
                     :on-click #(rf/dispatch [:set-logical-operator path key])}
            label]]))]
     (if (nil? indices)
       [build-numerical-clause path 1]
       (for [index indices]
         ^{:key (str "clause index " index)}
         [build-numerical-clause path index]))
     (when groups
       [build-extended-clauses (conj path :groups)])
     (when indices
       [:div
        [:div "Add clause"]
        [select/view factors #(let [{:keys [key]} %]
                                   (if (= key :groups)
                                     (rf/dispatch [:add-groups path])
                                     (rf/dispatch [:add-factor path (-> indices sort last inc)])))]])]))

(defn extended []
  (let [screener (rf/subscribe [:screener])
        securities (rf/subscribe [:securities])]
    [:div {:class "grid grid-cols-3 gap-10"}
     [:div
      [:div
       [:button {:on-click #(rf/dispatch [::intrinio.events/screen-securities @screener])}
        "REQUEST"]]
      [build-extended-clauses []]]
     (when @securities
       [:ul ;{:class "absolute bg-white w-full border rounded"}
        (for [{:keys [id ticker last-price currency asset-type name] :as value} @securities]
          ^{:key (str id)}
          [:li
           [:div {:class "flex flex-col justify-center h-20 border"}
                  ;:on-click #(do (rf/dispatch [:set-variable
                  ;                         (->> path (conj [:conditions]) flatten vec
                  ;                             (assoc variable-state :value (assoc value :label ticker)))])
                  ;               (swap! form-state assoc :blur? true))}
            [:div {:class "flex flex-wrap content-end font-semibold text-sm pl-2"} name]
            [:div {:class "flex flex-row items-end space-x-1 px-2"}
             [:div {:class "bg-gray-400 rounded font-semibold text-sm px-1"} ticker]
             [:div {:class "font-semibold text-sm"} (str "$" last-price)]
             [:div {:class "text-sm"} currency]
             [:div {:class "text-sm"} asset-type]]]])])]))

(defn securities-list []
  (let [securities (rf/subscribe [::intrinio.subs/securities])]
    (when (seq @securities)
      [:<>
       [:div {:class "flex justify-between items-center m-2"}
        [:span (str (count @securities) " found")]
        [:button {:class "border-2 border-black rounded-md px-1"
                  :on-click #(let [tickers (map :ticker @securities)]
                                  (rf/dispatch [::intrinio.events/reset-securities]))}
         "+ Add All"]]
       [:ul {:class "bg-modal w-full max-h-96 overflow-auto border rounded"}
        (for [{:keys [id ticker last-price currency type name] :as value} @securities]
             ^{:key (str id)}
             [:li {:class "hover:bg-white"}
              [:div {:class "flex justify-between items-center cursor-pointer"
                     :on-click #(rf/dispatch [::intrinio.events/remove-from-securities ticker])}
               [:div {:class "flex flex-col justify-center h-20 border-b mx-3"}
                [:div {:class "flex flex-wrap content-end font-semibold text-sm"} name]
                [:div {:class "flex flex-row items-end space-x-1"}
                 [:div {:class "bg-gray-400 rounded font-semibold text-sm px-1"} ticker]
                 [:div {:class "font-semibold text-sm"} (str "$" last-price)]
                 [:div {:class "text-sm"} currency]
                 [:div {:class "text-sm"} type]]]
               [:span {:class "mr-4 text-modal text-4xl"} "+"]]])]])))

(defn ticker-search []
  [:div {:class "flex flex-col"}
    [:div {:class "flex items-center justify-center border-b border-gray-500 bg-white w-full"}
     [:input {:id "tickers-search"
              :class "w-full m-4 p-2 rounded-sm border border-gray-500 bg-input text-sm text-gray-900"
              :placeholder "Search ticker or company name "
              :on-change #(let [s (-> % .-target .-value)]
                            (if (seq s)
                              (rf/dispatch [::intrinio.events/search-securities s])
                              (rf/dispatch [::intrinio.events/reset-securities])))}]]
   [securities-list]])

(defn build-basic-clauses [path]
  (let [screener (rf/subscribe [::screener.subs/screener])
        {:keys [operator clauses groups]} (if (seq path) (get-in @screener path) @screener)
        indices (keys clauses)]
    [:div {:class "flex flex-col pl-10"}
     (if (nil? indices)
       [build-numerical-clause path 1]
       (for [index indices]
         ^{:key (str "clause index " index)}
         [build-numerical-clause path index]))
     [:div
      [:button {:on-click #(rf/dispatch [::screener.events/add-metric path (-> indices sort last inc)])}
       "+"]]]))

(defn basic []
  [:<>
   [build-basic-clauses []]
   [securities-list]])

(defn indices []
  (let [indices (rf/subscribe [::intrinio.subs/indices])]
    (when @indices
      [:<>
       [select/view
        @indices
        #(rf/dispatch [::intrinio.events/set-index %])]
       [securities-list]])))

(defn view []
  (r/with-let [tab (r/atom :ticker-search)]
    {:header [:div {:class "space-x-4"}
              [:a {:class "cursor-pointer"
                   :on-click #(do (reset! tab :screener)
                                  (rf/dispatch [::screener.events/reset-screener])
                                  (rf/dispatch [::intrinio.events/reset-indices])
                                  (rf/dispatch [::intrinio.events/reset-securities]))}
               "SCREENER"]
              [:a {:class "cursor-pointer"
                   :on-click #(do (reset! tab :ticker-search)
                                  (rf/dispatch [::screener.events/reset-screener])
                                  (rf/dispatch [::intrinio.events/reset-indices])
                                  (rf/dispatch [::intrinio.events/reset-securities]))}
               "TICKER SEARCH"]
              [:a {:class "cursor-pointer"
                   :on-click #(do (reset! tab :indices)
                                  (rf/dispatch [::screener.events/reset-screener])
                                  (rf/dispatch [::intrinio.events/reset-indices])
                                  (rf/dispatch [::intrinio.events/reset-securities])
                                  (rf/dispatch [::intrinio.events/indices]))}
               "INDICES"]]
     :body   [:div
              (case @tab
                :ticker-search [ticker-search]
                :indices [indices]
                [basic])]}))
