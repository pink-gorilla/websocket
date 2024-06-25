(ns modular.ws.service.adapter.undertow
  (:require
   [taoensso.sente.server-adapters.undertow]))

(defn get-sch-adapter []
  (require '[taoensso.sente.server-adapters.undertow])
  taoensso.sente.server-adapters.undertow/get-sch-adapter)