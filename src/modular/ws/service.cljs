(ns modular.ws.service
  (:require
   [taoensso.timbre :as timbre :refer-macros [debug   warn]]
   [taoensso.sente :as sente]
   [taoensso.sente.packers.transit :as sente-transit] ;; Optional, for Transit encoding
   [transit.io :as e]
   [modular.ws.msg-handler :refer [event-msg-handler]]))

;; see: https://github.com/ptaoussanis/sente/blob/master/example-project/src/example/client.cljs

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(defn ws-init! [protocol path port]
  (debug "connecting sente websocket.. " protocol path port)
  (let [packer (sente-transit/get-transit-packer :json e/encode e/decode)
        opts {:type :auto  ; :ajax
              :ws-kalive-ms 7000
              :ws-ping-timeout-ms 30000
              :packer packer
              :protocol protocol}
        opts (if port
               (assoc opts :port port)
               opts)
        {:keys [chsk ch-recv send-fn state]} (sente/make-channel-socket-client!
                                              path; Must match server Ring routing URL
                                              ?csrf-token
                                              opts)]
    {:chsk chsk
     :ch-chsk ch-recv
     :chsk-send! send-fn
     :chsk-state state}))

; router

(defn sente-csrf-warning []
  (if ?csrf-token
    (debug "CSRF token detected in HTML, great!")
    (warn "CSRF token NOT detected in HTML, default Sente config will reject requests")))

(defn start-router! [conn]
  (let [{:keys [ch-chsk]} conn]
    (sente-csrf-warning)
    (sente/start-client-chsk-router! ch-chsk event-msg-handler)))

(defn  stop-router! [stop-f]
  (when stop-f
    (stop-f)))

(defn start-websocket-client! [protocol path port]
  (let [conn (ws-init! protocol path port)
        router (start-router! conn)]
    {:conn conn
     :router router}))

(defn stop-websocket-client! [{:keys [_conn router] :as _state}]
  (stop-router! router))

