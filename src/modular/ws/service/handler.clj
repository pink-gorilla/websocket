(ns modular.ws.service.handler
  (:require
   [taoensso.timbre :refer [debug debugf info infof]]
   [cheshire.core :as json]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [modular.ws.service.id :refer [get-sente-session-uid]]))

; CSRF TOKEN

(defn get-csrf-token []
  ; Another option:
  ;(:anti-forgery-token ring-req)] 
  (force *anti-forgery-token*))

; WEBSOCKET

(defn ws-token-handler-raw [_req]
  (let [token {:csrf-token (get-csrf-token)}]
    (info "sending csrf token: " token)
    {:status 200
     :body (json/generate-string token)})) ; json must stay! sente has issues if middleware converts this

(defn get-conn [{:keys [ctx] :as _req}]
  (get-in ctx [:sente :conn]))

(defn ws-handshake-handler [req]
  (debugf "ws/chsk GET: %s" req)
  (let [conn (get-conn req)
        {:keys [ring-ajax-get-or-ws-handshake]} conn
        client-id  (get-in req [:params :client-id]) ; check if sente requirements are met
        uid (get-sente-session-uid req)
        res (ring-ajax-get-or-ws-handshake req)]
    (infof "ws-chsk client-id: %s uid: %s csrf: %s"
           client-id uid
           (get-in req [:session :ring.middleware.anti-forgery/anti-forgery-token]))
    (debug res)
    res))

(defn ws-ajax-post-handler [req]
  (infof "ws/chsk POST: %s" req)
  (let [conn (get-conn req)
        {:keys [ring-ajax-post]} conn
        res (ring-ajax-post req)]
    (info "/chsk post result: " res)
    ;(info "ws csrf: " (get-in req [:session :ring.middleware.anti-forgery/anti-forgery-token]))
    res))