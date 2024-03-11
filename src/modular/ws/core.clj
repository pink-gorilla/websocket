(ns modular.ws.core
  (:require
   [taoensso.timbre :as log :refer [error info warn]]
   [modular.ws.ws :as ws]
   [modular.ws.service :as service]))

(defonce state-a (atom nil))

(defn start-websocket-server [server-type sente-debug?]
  (let [state (service/start-websocket-server server-type sente-debug?)]
    (reset! state-a state)
    state))

(defn stop-websocket-server [state]
  (service/stop-websocket-server state)
  (reset! state-a nil)
  nil)


(defn watch-conn [cb]
  (swap! (:watch @state-a) conj cb))

(defn send! [uid data]
  (if-let [conn (:conn @state-a)] 
    (ws/send! conn uid data)
    (error "ws/send - not setup. data: " data)))

(defn send-all! [data]
   (if-let [conn (:conn @state-a)] 
    (ws/send-all! conn data)
    (error "ws/send-all - not setup. data: " data)))

(defn connected-uids []
  (let [conn (:conn @state-a)
        {:keys [connected-uids]} conn
        uids (:any @connected-uids)]
    uids))



(defn send-response [{:as ev-msg :keys [id ?data ring-req ?reply-fn uid send-fn]}
                     msg-type response]
  ;(let [session (:session ring-req)
        ;uid (:uid session)
   ;     ]
   ; (when (nil? ?reply-fn)
   ;   (warn "reply-fn is nil. the client did chose to use messenging communication istead of req-res communication."))
    ;(warn "ws/session: " session)
    ;(if (nil? uid)
    ;  (warn "ws request uid is nil. ring-session not configured correctly.")
    ;  (info "ws/uid: " uid))
  (if (and msg-type response)
    (cond
      ?reply-fn (?reply-fn [msg-type response])
      uid (send! uid [msg-type response])
      :else (error "Cannot send ws-response: neither ?reply-fn nor uid was set!"))
    (error "Can not send ws-response - msg-type and response have to be set, msg-type:" msg-type "response: " response)))



(comment
  ;(println "clients: " @connected-uids)
  (send-all! [:demo/broadcast {:a 13}])

  ;
  )