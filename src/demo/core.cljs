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


(defn tab [active]
;; skipped for time
)

(defn tabs
  "Tabs don't do anything but you can style them like in the Figma doc"
  []
  [:div
    [:div "Tab 1"]
    [:div "Tab 2"]])

(defn dollar-input [input-val submit-val]
  [:form
    {:action "submit"}
  [:label {:for "value"} "Enter your value"]
  [:div
    {:class "group relative w-max mt-2"}
  [:div
    {:class "absolute inset-y-0 left-0 px-3 flex items-center text-gray-500"} 
    [:span
    {:class "pointer-events-none"}
    "$"
    ]
   ]
   [:input
    {:type :text
    :name "value"
    :id "value"
    ;; removing group hover as white on the btn doesn't look the best
     :class "pl-8 h-10 border border-gray-400 bg-gray-300 rounded-sm focus:bg-white focus:outline-none focus:border-black transition duration-150 group-hover:bg-gray-200 w-60"    
     :placeholder "0"
     :value @input-val
     :on-change (fn [e]
                  (let [val (-> e .-target .-value)]
                    (when (re-matches  #"[0-9.]+" val) ;; More rigorous validation in the future
                      (reset! input-val val))))
     :min 0}]
   [:button
    {
      :type "Submit"
      :aria-label "Submit"
      :class "absolute inset-y-0 right-0 flex items-center px-3 bg-blue-500 text-white border border-blue-600 rounded-sm hover:bg-blue-400 focus:outline-none focus:border-black transition duration-150"
      ;; button hovers is not accessible
      :on-click (fn [e]
                      (reset! submit-val @input-val)
                      (.preventDefault e))}
    "→"]]
    [:p {:class "text-gray-400 text-xs mt-3"} "Value can only be numerical and greater than 0"]])

(defn submitted-value-output [dollar-val]
  [:div
    {:class "w-96"}
  [:h1
  {:class "mb-2"}
    "The value you entered"
    ]
    [:div 
      {:class "grid grid-cols-3 gap-y-2"}
    [:div 
      {:class "col-span-2 h-10 border-b-2 border-t-2 flex items-center"}
      [:h2
        "Title"
      ]
    ]

    [:div 
      {:class "h-10 flex border-b-2 border-t-2 items-center"}
      [:h2
        "Value"
      ]
    ]

    [:div
      {:class "col-span-2"}
        [:h3
          {:class "font-medium"}
          "VAL"
        ]
        [:p
          {:class "font-normal text-gray-500"}
          "Company Title"
        ]
      ]
    [:div
      {:class "flex items-center"}
      [:span "$" dollar-val]
    ]
    ]])

(defn root
  "You shouldn't need to mess with this component too much."
  []
  (r/with-let
    [dollars (r/atom nil)
     submitted-dollars (r/atom nil)]
    [:div
      [task-description]
      [:br]
      [tabs]
      [:br]
      [:div 
        {:class "flex flex-wrap md:flex-nowrap gap-10 w-full"}
      [dollar-input dollars submitted-dollars]
      (when @submitted-dollars [submitted-value-output @submitted-dollars])]]))

(defn app-start!
  "Initiates the cljs application"
  []
  (rdom/render [root] (js/document.getElementById "tgt-div")))

