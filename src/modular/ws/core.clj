(ns modular.ws.core
  (:require
   [taoensso.timbre :as log :refer [debugf info warn error]]
   [taoensso.sente  :as sente]
   [modular.ws.service.adapter :as adapter]
   [modular.ws.service.handler :as handler]
   [modular.ws.service.router :as router]
   [modular.ws.service.watch :as watch]))

;; SERVICE START/STOP

(defn start-websocket-server [server-type sente-debug?]
  (let [conn (adapter/ws-init! server-type)
        bidi-routes (handler/create-bidi-routes conn)
        router (router/start-router! conn)
        watch (watch/watch-conn-start conn)]
    (when sente-debug?
      (reset! sente/debug-mode?_ true))
    {:conn conn
     :bidi-routes bidi-routes
     :router router
     :watch watch}))

(defn stop-websocket-server [{:keys [conn bidi-routes router watch] :as this}]
  (router/stop-router! router))

;; SEND 

(defn send! [{:keys [conn] :as _this} uid data]
  (let [{:keys [chsk-send!]} conn]
    (when-not (= uid :sente/nil-uid)
      (chsk-send! uid data))))

(defn connected-uids [{:keys [conn]}]
  (let [{:keys [connected-uids]} conn]
    (:any @connected-uids)))

(defn send-all! [this data]
  (let [uids (connected-uids this)
        nr (count uids)]
    (when (> nr 0)
      (debugf "Broadcasting event type: %s to %s clients" (first data) nr)
      (doseq [uid uids]
        (send! this uid data)))))