;   Copyrigh (c) Alan Thompson. All rights reserved.
;   The use and distribution terms for this software are covered by the Eclipse Public License 1.0
;   (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at
;   the root of this distribution.  By using this software in any fashion, you are agreeing to be
;   bound by the terms of this license.  You must not remove this notice, or any other, from this
;   software.
(ns
  ^:figwheel-hooks ; metadata tells Figwheel.Main to find & call reload hook fn's are present
  demo.core
  (:require
    [demo.commons.modal :as modal]
    [demo.components.screener.view :as screener]
    [demo.events :as events]
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [re-frame.core :as rf]
    ))

(defn root []
  [modal/view (screener/view)])

(defn app-start!
  "Initiates the cljs application"
  []
  (println "app-start - enter")
  (rf/dispatch-sync [:initialize])
  (rdom/render [root] (js/document.getElementById "app"))
  (println "app-start - leave"))

