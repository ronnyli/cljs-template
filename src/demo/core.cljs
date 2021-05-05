(ns
  demo.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    ))

(defn task-description []
  [:div
    [:p "This page contains an input field and a button."]
    [:p "Clicking Submit will display the submitted value on the screen."]
    [:p
      [:span "Please modify the code such that it matches the "]
      [:a {:class "text-blue-600" :target "_blank" :href "https://www.figma.com/file/uLi16y671HfCp4FNrPB6HA/UX-Dev-Test?node-id=0%3A1"}
        "Figma design"]
      [:span " and provides a better user experience."]]])

(defn tabs
  "Tabs don't do anything but you can style them like in the Figma doc"
  []
  [:div
    [:div "Tab 1"]
    [:div "Tab 2"]])

(defn dollar-input [input-val submit-val]
  [:div
   "$"
   [:input
    {:type :text
     :value @input-val
     :on-change (fn [e]
                  (let [val (-> e .-target .-value)]
                    (when (re-matches  #"[0-9.]+" val) ;; More rigorous validation in the future
                      (reset! input-val val))))
     :min 0}]
   [:div
    {:class "text-blue-700"
      :on-click (fn [e]
                      (reset! submit-val @input-val)
                      (.preventDefault e))}
    "Submit"]])

(defn submitted-value-output [dollar-val]
  [:div
    "The value you entered"
    [:br]
    "Title"
    "Value"
    [:div
      "VAL"
      "Company Title"]
    [:span "$" dollar-val]])

(defn root
  "You shouldn't need to mess with this component too much."
  []
  (r/with-let
    [dollars (r/atom 1000)
     submitted-dollars (r/atom nil)]
    [:div
      [task-description]
      [:br]
      [tabs]
      [:br]
      [dollar-input dollars submitted-dollars]
      [:br]
      (when @submitted-dollars [submitted-value-output @submitted-dollars])]))

(defn app-start!
  "Initiates the cljs application"
  []
  (rdom/render [root] (js/document.getElementById "tgt-div")))

