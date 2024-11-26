(ns modular.ws.service.middleware
  (:require
   [clojure.string]
   ;[ring.util.response :refer [response]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   ;[ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.session :refer [wrap-session]]
  ; [muuntaja.middleware :refer [wrap-format]] ; 30x faster than ring.middleware.format
   [modular.webserver.middleware.api :as api]))

#_(defn wrap-formats2 [handler]
    (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
      (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
        ((if (:websocket? request) handler wrapped) request))))

(def wrap-api-handler api/wrap-api-handler)

#_(defn wrap-ws [handler]
    (-> handler
        #_(wrap-defaults
           (-> site-defaults
               (assoc-in [:security :anti-forgery] true)))
        (wrap-defaults api-defaults)
      ;(wrap-api-handler)
      ;(wrap-cors-handler)
      ;(wrap-session)
      ;(wrap-json-response)
        (wrap-gzip))) ;oz 

(defn wrap-ws [handler]
  (-> handler
      ;allow-cross-origin
      (wrap-defaults site-defaults)
      (wrap-session)
      wrap-keyword-params
      wrap-params
       ; needed to query remote apis from cljs
      #_(wrap-cors :access-control-allow-origin [#".*"]
                   :access-control-allow-methods [:get :put :post :delete])
      (wrap-gzip)))
