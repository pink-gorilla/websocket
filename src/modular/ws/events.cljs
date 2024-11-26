(ns modular.ws.events
  (:require
   [taoensso.timbre :refer-macros [debug info error]]
   [re-frame.core :as rf]))

(rf/reg-event-db
 :ws/state
 (fn [db [_ new-state-map _old-state-map]]
   (debug "ws/state " new-state-map)
   (when (:first-open? new-state-map)
     (info "ws open (first-time): " new-state-map)
     (rf/dispatch [:ws/open-first new-state-map]))
   (assoc db :ws new-state-map)))

(rf/reg-sub
 :ws/connected?
 (fn [db _]
   (get-in db [:ws :open?])))

(rf/reg-event-db
 :ws/unknown
 (fn [db [_ data]]
   (error "ws server not setup to handle events of type: " data)
   db))

