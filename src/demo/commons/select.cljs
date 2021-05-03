(ns demo.commons.select
  (:require
    [reagent.core :as r]
    ;;
    [demo.commons.icons :as icons]))

(defn view [{:keys [id placeholder values value select options]} on-click]
  "tailwind select"
  (r/with-let [show? (r/atom false)]
    (let [{:keys [left-icon label right-icon]} value
          {:keys [style left-icon-style label-style right-icon-style]} select
          {:keys [form-style option-style]} options]
      [:div {:id id}
       [:button {:id            "options-menu"
                 :class         (str  "relative " style)
                 :type          "button"
                 :aria-haspopup "true"
                 :aria-expanded "true"
                 :on-click      #(do (.preventDefault %)
                                     (reset! show? true))}
        (if left-icon
          [:div {:class "flex flex-row"}
           [:div {:class left-icon-style} left-icon]
           [:div {:class label-style} label]
           [:div {:class right-icon-style} (or right-icon icons/angle-down)]]
          [:div {:class "flex flex-row w-full"}
           [:div {:class label-style} (or label placeholder)]
           [:div {:class right-icon-style} (or right-icon icons/angle-down)]])]
       (when @show?
         [:div {:class form-style}
          [:div {:class            "py-1"
                 :role             "menu"
                 :aria-orientation "vertical"
                 :aria-labelledby  "options-menu"}
           (for [menu-item values]
             ^{:key (str (:label menu-item))}
             [:div {:role     "menuitem"
                    :class    option-style
                    :on-click #(do (.preventDefault %)
                                   (.stopPropagation %)
                                   (reset! show? false)
                                   (on-click menu-item))}
              (:label menu-item)])]])])))
