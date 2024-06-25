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

;; REPLY

(defn send-response [{:as ev-msg :keys [id ?data ring-req ?reply-fn uid send-fn]}
                     msg-type response]
  ;(let [session (:session ring-req)
        ;uid (:uid session)
   ;     ]
  ;(when (nil? ?reply-fn)
   ; (warn "reply-fn is nil. the client did chose to use messenging communication istead of req-res communication."))
    ;(warn "ws/session: " session)
    ;(if (nil? uid)
    ;  (warn "ws request uid is nil. ring-session not configured correctly.")
    ;  (info "ws/uid: " uid))
  (if (and msg-type response)
    (cond
      ?reply-fn (?reply-fn [msg-type response])
      uid (send-fn uid [msg-type response])
      :else (error "Cannot send ws-response: neither ?reply-fn nor uid was set!"))
    (error "Can not send ws-response - msg-type and response have to be set, msg-type:" msg-type "response: " response)))

;; WATCH

(defn connected-uids [this]
  (let [conn (:conn this)
        {:keys [connected-uids]} conn
        uids (:any @connected-uids)]
    uids))






