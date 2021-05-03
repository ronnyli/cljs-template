(ns demo.components.utils)

(defn target->integer [target]
      (-> target
          .-target
          .-value
          js/parseInt
          ((fn [v] (when-not (js/isNaN v) v)))))
