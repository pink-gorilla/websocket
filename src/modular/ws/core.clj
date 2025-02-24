(ns modular.ws.core
  (:require
   [taoensso.timbre :as log :refer [debugf]]
   [modular.ws.service.adapter :as adapter]
   [modular.ws.service.router :as router]
   [modular.ws.service.watch :as watch]))

;; SERVICE START/STOP

(defn start-websocket-server [server-type]
  (let [conn (adapter/ws-init! server-type)
        router (router/start-router! conn)
        watch (watch/watch-conn-start conn)]
    {:conn conn
     :router router
     :watch watch}))

(defn stop-websocket-server [{:keys [_conn _bidi-routes router _watch] :as _this}]
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