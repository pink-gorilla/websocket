(ns modular.ws.msg-handler
  (:require
   [taoensso.timbre :refer [debugf  infof   error errorf]]))

(defn ws-reply [{:keys [_event _id _?data _ring-req ?reply-fn _send-fn] :as _req}
                res]
  (when ?reply-fn
    (?reply-fn res)))

;; REPLY

(defn send-response [{:as _msg :keys [_id _?data _ring-req ?reply-fn uid send-fn]}
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

(defmulti -event-msg-handler :id)

(defmethod -event-msg-handler :chsk/uidport-open
  [{:as _msg :keys [event _id _?data _ring-req _?reply-fn _send-fn]}]
  (infof ":chsk/uidport-open: %s" event))

(defmethod -event-msg-handler :chsk/uidport-close
  [{:as _msg :keys [event _id _?data _ring-req _?reply-fn _send-fn]}]
  (infof ":chsk/uidport-close: %s" event))

(defmethod -event-msg-handler :chsk/ws-ping
  [{:as _msg :keys [event _id _?data _ring-req ?reply-fn _send-fn]}]
  (infof ":chsk/ws-ping: %s" event)
  (when ?reply-fn
    (?reply-fn {:unmatched-event-as-echoed-from-server event})))

(defmethod -event-msg-handler :chsk/ws-pong
  [{:as _msg :keys [event _id _?data _ring-req ?reply-fn _send-fn]}]
  (infof ":chsk/ws-pong: %s" event)
  (when ?reply-fn
    (?reply-fn {:unmatched-event-as-echoed-from-server event})))

(defmethod -event-msg-handler :chsk/bad-package
  [{:as _msg :keys [event _id _?data _ring-req _?reply-fn _send-fn]}]
  (infof ":chsk/bad-package: %s" event))

(defmethod -event-msg-handler :chsk/bad-event
  [{:as _msg :keys [event _id _?data _ring-req _?reply-fn _send-fn]}]
  (infof ":chsk/bad-event: %s" event))

(defmethod -event-msg-handler :default
  [{:keys [event id _?data ring-req _?reply-fn _send-fn] :as msg}]
  (let [session (:session ring-req)
        _uid (:uid session)]
    (errorf "received websocket message of unknown type: %s event: %s" id event)
    (send-response msg :ws/unknown event)))

(defn event-msg-handler [{:keys [_client-id id event ?data _uid] :as msg}]
  (debugf "ws rcvd: evt: %s id: %s data: %s" event id ?data)
  (when msg
    (-event-msg-handler msg)))

; {:client-id "591b690d-5633-48c3-884d-348bbcf5c9ca"
; :uid "3c8e0a40-356c-4426-9391-1445140ff509"
; :event [:chsk/uidport-close "3c8e0a40-356c-4426-9391-1445140ff509"]
; :id :chsk/uidport-close 
; :?data "3c8e0a40-356c-4426-9391-1445140ff509"
; :?reply-fn nil, 
; :connected-uids #object[clojure.lang.Atom 0x76b6a6d1 
; {:status :ready, :val {:ws #{}, 
;                        :ajax #{"82c82316-9c01-4abf-8046-e6e676246468"}, 
;                        :any #{"82c82316-9c01-4abf-8046-e6e676246468"}}}], 

