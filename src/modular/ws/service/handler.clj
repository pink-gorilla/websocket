(ns modular.ws.service.handler
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn error]]
   [cheshire.core :as json]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

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
  (if-let [conn (get-conn req)]
    (let [{:keys [ring-ajax-get-or-ws-handshake]} conn
          res (ring-ajax-get-or-ws-handshake req)]
      (info "ws-chsk csrf: anti-forgery-token: " (get-in req [:session :ring.middleware.anti-forgery/anti-forgery-token])) 
      res
      )
    (error "sente ws-handshake error. you need to set :sente in the context")))

(defn ws-ajax-post-handler [req]
    (info "/chsk post")
  (let [conn (get-conn req)
        {:keys [ring-ajax-post]} conn
        res (ring-ajax-post req)]
    ;(info "ws csrf: " (get-in req [:session :ring.middleware.anti-forgery/anti-forgery-token]))
    res))