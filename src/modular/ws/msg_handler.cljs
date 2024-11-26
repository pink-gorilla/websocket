(ns modular.ws.msg-handler
  (:require
   [taoensso.timbre :as timbre :refer-macros [debug debugf infof    error]]
   [re-frame.core :as rf]
   [taoensso.encore :as encore :refer-macros [have]]))

(defmulti -event-msg-handler :id)

(defmethod -event-msg-handler :chsk/handshake
  [{:as _msg :keys [?data]}]
  (let [[_?uid _?csrf-token _?handshake-data] ?data]
    (debugf "Handshake: %s" ?data)))

(defmethod -event-msg-handler :chsk/state
  [{:keys [?data] :as _msg}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (debugf "ws state: %s" new-state-map)
    (rf/dispatch [:ws/state new-state-map old-state-map])))

(defmethod -event-msg-handler :chsk/ws-ping
  [{:as _msg :keys [event _id _?data _ring-req _?reply-fn _send-fn]}]
  (infof ":chsk/ws-ping: %s" event))

(defmethod -event-msg-handler :default
  [{:as _msg :keys [event _?data]}]
  (infof "forwarding to reframe: %s" event)
  (if (vector? event)
    (do
      (debug "dispatching rcvd ws msg to reframe:" event)
      (rf/dispatch event))
    (error "ws rcvd. cannot dispatch. data no vector: " event)))

; msg-handler

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:keys [id ?data event] :as req}]
  (debugf "ws rcvd: evt: %s id: %s data: %s" event id ?data)
  (-event-msg-handler req))