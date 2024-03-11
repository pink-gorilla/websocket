(ns modular.ws.service
   (:require
    [taoensso.sente  :as sente]
    [modular.ws.service.adapter :as adapter]
    [modular.ws.service.handler :as handler]
    [modular.ws.service.router :as router]
    [modular.ws.service.watch :as watch]))

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

(defn stop-websocket-server [{:keys [conn bidi-routes router watch] :as state}]
  (router/stop-router! router))