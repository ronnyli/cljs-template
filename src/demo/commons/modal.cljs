(ns demo.commons.modal
  (:require
   [demo.commons.icons :as icons]))

(defn view [{:keys [header close-fn delete-fn body remainder assign-remainder-fn]}]
  [:div
   [:div {:class    "justify-center items-center flex overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none"
          :on-click #(do
                       (.preventDefault %)
                       (.log js/console "setShowModal(Inside)"))}
    [:div {:class "relative w-auto mx-auto max-w-3xl"}
     [:div {:class "border-0 rounded-md shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none"}
      [:div {:class "flex justify-between px-5 py-3 border-b border-none border-gray-300 rounded-t text-sm"}
       header
       [:button {:type     "button"
                 :class    "ml-5 justify-self-start"
                 :on-click close-fn}
        [:i icons/close]]]
      [:div {:class "relative flex-auto"}
       [:div {:class "text-gray-600 text-lg leading-relaxed"}
        body]]]]]
   [:div {:class "opacity-25 fixed inset-0 z-40 bg-black"
          :on-click #(do
                       (.preventDefault %)
                       (.log js/console "setShowModal(Outside)"))}]])
