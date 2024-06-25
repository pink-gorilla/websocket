(ns modular.ws.core
  (:require
   [taoensso.timbre :refer-macros [debug info error]]
   [re-frame.core :as rf]
   [modular.ws.service :as service]
   [modular.ws.ws :as ws]))

(defonce state (atom nil))

(defn start-websocket-client! [path port]
  (let [s (service/start-websocket-client! path port)]
    (reset! state s)
    s))

(defn stop-websocket-client! [state]
  (service/stop-websocket-client! state)
  (reset! state nil)
  nil)

(defn send!
  ([data]
   (when data
     (info "sending (no cb): " data)
     (try
       (ws/send (:conn @state) data)
       (catch :default e
         (error "exception sending to ws: " e)))))
  ([data cb timeout]
   (when data
     (info "sending (cb): " data)
     (try
       (ws/send (:conn @state) data cb timeout)
       (catch :default e
         (error "exception sending to ws: " e))))))

(rf/reg-event-db
 :ws/send
 (fn [db v]
   (case (count v)
     2 (let [[_ data] v]
         (info "ws send (no cb): " data)
         (send! data))
     4 (let [[_ data cb timeout] v]
         (info "ws send (cb): " data)
         (send! data cb timeout))
     (error ":ws/send bad format: " v))
   db))