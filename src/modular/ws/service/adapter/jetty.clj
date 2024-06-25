(ns modular.ws.service.adapter.jetty
  (:require
   [taoensso.sente.server-adapters.jetty9]))

(defn get-sch-adapter []
  (require '[taoensso.sente.server-adapters.jetty9])
  taoensso.sente.server-adapters.jetty9/get-sch-adapter)