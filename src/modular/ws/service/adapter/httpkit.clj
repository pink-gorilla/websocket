(ns modular.ws.service.adapter.httpkit
  (:require
   [taoensso.sente.server-adapters.http-kit]))

(defn get-sch-adapter []
  (require '[taoensso.sente.server-adapters.http-kit])
  taoensso.sente.server-adapters.http-kit/get-sch-adapter)