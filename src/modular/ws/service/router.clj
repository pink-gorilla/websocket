(ns modular.ws.service.router
 (:require
  [taoensso.sente  :as sente]
  [modular.ws.msg-handler :refer [event-msg-handler]]))

; router

(defn stop-router! [stop-fn]
  (when stop-fn 
    (stop-fn)))

(defn start-router! [conn]
  (let [{:keys [ch-chsk]} conn]
    (sente/start-server-chsk-router! ch-chsk event-msg-handler)))
