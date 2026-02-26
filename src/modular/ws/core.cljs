(ns modular.ws.core
  (:require
   [taoensso.timbre :refer-macros [info warn error]]
   [re-frame.core :as rf]
   [modular.ws.service :as service]
   [modular.ws.ws :as ws]))

(defonce state (atom nil))

(defonce settings-a (atom {}))

(defn start-websocket-client! [protocol path port]
  (reset! settings-a {:protocol protocol :path path :port port})
  (let [s (service/start-websocket-client! protocol path port)]
    (reset! state s)
    s))

(defn stop-websocket-client! [state]
  (service/stop-websocket-client! state)
  (reset! state nil)
  nil)

(defn restart-websocket-client! []
  (warn "restarting websocket client ...")
  (let [{:keys [protocol path port]} @settings-a]
    (stop-websocket-client! @state)
    (start-websocket-client! protocol path port))
  (warn "restarting websocket client finished!"))

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

; todo: remove :ws/send
; 59 goldly/src-unused/system/goldly/component/ui.cljs
; 11 goldly/src-unused/system/goldly/notebook_loader/clj_load.cljs
; 15 devtools/devtools/src/goldly/devtools/websocket.cljs
; 108 oauth2/src/token/identity/dialog.cljs

