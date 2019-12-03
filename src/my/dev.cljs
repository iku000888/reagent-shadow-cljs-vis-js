(ns my.dev
  (:require
   [reagent.core :as reagent]
   ["vis-network" :as vis]))

(defn graph [{:keys [nodes edges
                     on-edge-add
                     add-edge-mode]}]
  [:div
   {:ref (fn [dom]
           (when dom
             (let [parent-dom (.-parentElement dom)
                   nw (vis/Network.
                       dom
                       #js {:nodes (vis/DataSet. (clj->js nodes))
                            :edges (vis/DataSet. (clj->js edges))}
                       (clj->js
                        {:layout {:randomSeed 111} ;;Make deterministic
                         :edges {:arrows "to"
                                 :color "red"
                                 :physics false}
                         :manipulation
                         {:addEdge (fn [data callback]
                                     (on-edge-add {:from (.-from data)
                                                   :to (.-to data)})
                                     (callback data))}}))]
               (when add-edge-mode
                 (.addEdgeMode nw))
               (doto nw
                 (.setSize
                  (.-clientWidth parent-dom)
                  (.-clientHeight parent-dom))
                 (.fit (clj->js (map :id nodes)))))))}])

(defn view []
  (reagent/with-let
    [state (reagent/atom {})]
    [:div
     [:button
      {:on-click #(swap! state
                         update
                         :nodes
                         conj
                         {:id (count (:nodes @state))
                          :label (str (count (:nodes @state)))})}
      "Add Node"]
     [graph {:nodes (:nodes @state)
             :edges (:edges @state)
             :add-edge-mode true
             :on-edge-add (fn [edge]
                            (swap! state update :edges conj edge))}]]))

(defn render-view []
  (reagent/render [view]
                  (js/document.getElementById "root")))

(defn ^:dev/after-load start []
  (js/console.log "start")
  (render-view))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (render-view))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
