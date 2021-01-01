(ns prayer-bot.core
  (:require [clojure.core.async :refer [chan close!]]
            [clojure.edn :as edn]
            [discljord.connections :as discord-ws]
            [discljord.events :refer [message-pump!]]
            [discljord.formatting :refer [mention-user]]
            [discljord.messaging :as discord-rest]
            [dk.ative.docjure.spreadsheet :as spreadsheet]))

(def state (atom nil))

(def bot-id (atom nil))

(def config (edn/read-string (slurp "config.edn")))

(def discord-token (System/getenv "DISCORD_TOKEN"))

(defn read-rows []
  (->>
    (spreadsheet/load-workbook "resources/quotes.xlsx")
    (spreadsheet/select-sheet "Sheet1")
    (spreadsheet/select-columns {:A :quote :B :group})
    ))

(defn valid-row? [col]
  (not (nil? (:quote col))))

(defn all-valid-rows []
  (->>
    (read-rows)
    (map #(update % :group (comp set list)))
    (filter valid-row?)
    ))

(def quotes (all-valid-rows))

(defmulti handle-event (fn [type _data] type))

;; (defn random-response [user]
;;   (str (rand-nth (:responses config)) ", " (mention-user user) \!))

(defn random-quote [user]
  (str (:quote (rand-nth quotes)) ", " (mention-user user) \!))

(defmethod handle-event :message-create
  [_ {:keys [channel-id author mentions] :as _data}]
  (when (some #{@bot-id} (map :id mentions))
    (discord-rest/create-message! (:rest @state) channel-id :content (random-quote author))))

(defmethod handle-event :ready
  [_ _]
  (discord-ws/status-update! (:gateway @state) :activity (discord-ws/create-activity :name (:playing config))))

(defmethod handle-event :default [_ _])

(defn start-bot! [token & intents]
  (let [event-channel (chan 100)
        gateway-connection (discord-ws/connect-bot! token event-channel :intents (set intents))
        rest-connection (discord-rest/start-connection! token)]
    {:events  event-channel
     :gateway gateway-connection
     :rest    rest-connection}))

(defn stop-bot! [{:keys [rest gateway events] :as _state}]
  (discord-rest/stop-connection! rest)
  (discord-ws/disconnect-bot! gateway)
  (close! events))

(defn -main [& args]
  (reset! state (start-bot! discord-token :guild-messages))
  (reset! bot-id (:id @(discord-rest/get-current-user! (:rest @state))))
  (try
    (message-pump! (:events @state) handle-event)
    (finally (stop-bot! @state))))

(comment
  (-main)
  
  (reset! state (start-bot! discord-token :guild-messages))
  (stop-bot! @state)
  )
