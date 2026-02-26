(ns modular.ws.service.adapter
  (:require
   [taoensso.sente.packers.transit :as sente-transit]
   [taoensso.sente  :as sente]
   [transit.io :as e]
   [modular.ws.service.id :refer [sente-user-id-fn]]))

(defn get-adapter [server-type]
  (let [s (case server-type
            :untertow 'taoensso.sente.server-adapters.community.undertow/get-sch-adapter
            :jetty 'taoensso.sente.server-adapters.community.jetty/get-sch-adapter
            :httpkit 'taoensso.sente.server-adapters.http-kit/get-sch-adapter)
        get-sch-adapter (requiring-resolve s)]
    get-sch-adapter))

(defn ws-init! [server-type]
  (let [get-sch-adapter (get-adapter server-type)
        encode (e/write-opts)
        decode (e/read-opts)
        packer (sente-transit/get-transit-packer :json encode decode)
        chsk-server (sente/make-channel-socket-server!
                     (get-sch-adapter)
                     {:packer packer
                      :ws-kalive-ms 15000
                      :ws-ping-timeout-ms 50000
                      :csrf-token-fn nil ; awb99; disable CSRF checking.
                      :user-id-fn sente-user-id-fn})
        {:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
    {:chsk-send! send-fn
     :ch-chsk ch-recv
     :connected-uids connected-uids
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
     :ring-ajax-post ajax-post-fn}))