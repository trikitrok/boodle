(ns boodle.transactions.events
  (:require [boodle.ajax :as ajax]
            [boodle.modal :as modal]
            [boodle.validation :as v]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :load-active-aim-transactions
 (fn [db [_ result]]
   (assoc-in db [:aims :aim :active :transactions] result)))

(rf/reg-event-db
 :load-achieved-aim-transactions
 (fn [db [_ result]]
   (assoc-in db [:aims :aim :achieved :transactions] result)))

(rf/reg-event-fx
 :get-aim-transactions
 (fn [{db :db} [_ value]]
   (assoc
    (ajax/get-request (str "/api/transaction/aim/" value)
                      [:load-active-aim-transactions]
                      [:bad-response])
    :db (assoc-in db [:aims :params :active] value))))

(rf/reg-event-db
 :transaction-change-item
 (fn [db [_ value]]
   (assoc-in db [:aims :transactions :row :item] value)))

(rf/reg-event-db
 :transaction-change-amount
 (fn [db [_ value]]
   (assoc-in db [:aims :transactions :row :amount] value)))

(defn validate-item
  [transaction]
  (v/validate-input
   (:item transaction)
   [{:message "Motivo: è obbligatorio"
     :check-fn v/not-empty?}]))

(defn validate-amount
  [transaction]
  (v/validate-input
   (:amount transaction)
   [{:message "Importo: deve essere un numero (es.: 3,55)"
     :check-fn v/valid-amount?}]))

(defn validate-transaction
  [transaction]
  (let [result []]
    (-> result
        (into (validate-item transaction))
        (into (validate-amount transaction)))))

(rf/reg-event-fx
 :create-transaction
 (fn [{db :db} [_ id-aim]]
   {:db (assoc-in db [:aims :transactions :row] nil)
    :dispatch
    [:modal
     {:show? true
      :child [modal/save-transaction
              "Crea movimento"
              [:save-transaction id-aim]]}]}))

(rf/reg-event-fx
 :save-transaction
 (fn [{db :db} [_ id-aim]]
   (let [transaction (get-in db [:aims :transactions :row])
         not-valid (validate-transaction transaction)]
     (if-not (empty? not-valid)
       (rf/dispatch [:validation-error not-valid])
       (assoc
        (ajax/post-request "/api/transaction/insert"
                           (assoc transaction :id-aim id-aim)
                           [:get-aim-transactions id-aim]
                           [:bad-response])
        :db (assoc db :show-validation false)
        :dispatch [:modal {:show? false :child nil}])))))
