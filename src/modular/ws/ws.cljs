(ns modular.ws.ws
  (:require
   [taoensso.timbre :as timbre :refer-macros [debug debugf error]]
   [re-frame.core :as rf]))

(defn- cb-dispatch-to-reframe
  [cb-reply]
  (debugf "dispatching ws callback reply: %s" cb-reply)
  (if (vector? cb-reply)
    (do
      (debug "dispatching cb: " cb-reply)
      (rf/dispatch cb-reply))
    (error "ws reply/dispatch to reframe failed. not a vector: " cb-reply)))

(defn send
  ([this data]
   (let [{:keys [chsk-send!]} this]
     (debug "chsk-send!")
     (if chsk-send!
       (chsk-send! data)  ; sente send callbacks dont work with reframe
       (error "chsk-send! not defined! cannot send: " data))))
  ([this data cb timeout]
   (let [{:keys [chsk-send!]} this]
     (debug "chsk-send!")
     (if chsk-send!
       (chsk-send! data timeout cb)  ; sente send callbacks dont work with reframe
       (error "chsk-send! not defined! cannot send: " data)))))
