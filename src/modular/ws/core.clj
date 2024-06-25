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

(defn send! [this uid data]
  (let [{:keys [chsk-send!]} this]
    (when-not (= uid :sente/nil-uid)
      (chsk-send! uid data))))

(defn send-all! [this data]
  (let [{:keys [connected-uids]} this
        uids (:any @connected-uids)
        nr (count uids)]
    (when (> nr 0)
      (debugf "Broadcasting event type: %s to %s clients" (first data) nr)
      (doseq [uid uids]
        (send! this uid data)))))

;; WATCH

(defn connected-uids [this]
  (let [conn (:conn this)
        {:keys [connected-uids]} conn
        uids (:any @connected-uids)]
    uids))






