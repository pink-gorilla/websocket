(ns modular.ws.service.id
  (:require
   [nano-id.core :refer [nano-id]]))

(defn unique-id
  "Get a unique id for a session."
  []
  (nano-id 8))

(defn session-uid
  "Get session uuid from a request."
  [req]
  (get-in req [:session :uid]))

(defn get-sente-session-uid
  "Get session uuid from a request."
  [req]
  (or (session-uid req)
      (unique-id)))

(defn sente-session-with-uid [req]
  (let [session (or (:session req) {})
        uid (or (get-sente-session-uid req)
                (unique-id))]
    (assoc session :uid uid)))