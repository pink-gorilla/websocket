(ns modular.ws.service.watch)

(defn watch-conn-start [this]
  (let [watcher-cbs (atom [])
        {:keys [connected-uids]} this]
    (add-watch connected-uids :connected-uids
               (fn [_ _ old new]
                 (when (not= old new)
                   (doall (for [cb @watcher-cbs]
                            (cb old new))))))
    watcher-cbs))

