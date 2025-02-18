(ns modular.ws.wsconfig)

(defn config-ws [_module-name config _exts _default-config]
  (select-keys config [:ports :mode]))