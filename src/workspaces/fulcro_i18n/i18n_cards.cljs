(ns fulcro-i18n.i18n-cards
  (:require
    [com.fulcrologic.fulcro-i18n.i18n :as i18n :refer [tr]]
    [com.fulcrologic.fulcro-i18n.icu-formatter :as icu]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [com.fulcrologic.fulcro.react.version18 :refer [react18-options]]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [taoensso.timbre :as log]))

(declare AltRootPlainClass app)

(defsc OtherChild [this {:keys [:other/id :other/n] :as props}]
  {:query         [:other/id :other/n]
   :ident         :other/id
   :initial-state {:other/id :param/id :other/n :param/n}}
  (dom/div
    (dom/button
      {:onClick #(m/set-integer! this :other/n :value (inc n))}
      (str n))))

(def ui-other-child (comp/factory OtherChild {:keyfn :other/id}))

(defsc SomeHookChild [this {:child/keys [id label other]}]
  {:query         [:child/id :child/label
                   {:child/other (comp/get-query OtherChild)}]
   :initial-state {:child/id    :param/id
                   :child/label (str "some child")
                   :child/other {:id 42 :n 1000}}
   :ident         :child/id}
  (dom/div
    (dom/div (tr "Child") label)
    (ui-other-child other)
    (dom/button {:onClick #(m/set-string! this :child/label :value "RESET")} "Reset")
    (dom/input {:value    label
                :onChange (fn [evt] (m/set-string! this :child/label :event evt))})))

(def ui-some-hook-child (comp/factory SomeHookChild {:keyfn :child/id}))

(defsc Hook [this {:hook/keys [id x child] :as props}]
  {:query         [:hook/id :hook/x {:hook/child (comp/get-query SomeHookChild)}]
   :ident         :hook/id
   :initial-state (fn [{:keys [id child-id]}]
                    {:hook/x     1
                     :hook/id    id
                     :hook/child (comp/get-initial-state SomeHookChild {:id child-id})})
   :use-hooks?    true}
  (let [[v set-v!] (hooks/use-state 140)]
    (dom/div (tr "This is a hooks-based component: ")
      (ui-some-hook-child child)
      (dom/button {:onClick #(set-v! (inc v))} (str v))
      (dom/button {:onClick #(m/set-integer! this :hook/x :value (inc x))}
        (str x)))))

(def ui-hook (comp/factory Hook {:keyfn :hook/id}))

(def built-in-translations
  ;; The keys are [context original-string] and the values are the translation.
  ;; (tr "Hello") -> {["" "Hello"] "Hola"}
  ;; (trc "Abbreviation for male" "M") -> {["Abbreviation for male" "M"] "Translation"}
  ;;
  ;; Normally these would be auto-extracted into a PO file and sent to a translator, but since we don't use
  ;; Fulcro i18n and there are not many I figured this would be good enough. TK
  {["" "A"]        "Aay"
   ["" "B"]        "Bee"
   ["" "Children"] "CHILDREN"})

(defsc Root [this {:keys [hook hooks normal-child] :as props}]
  {:query         [{:hook (comp/get-query Hook)}
                   {:hooks (comp/get-query Hook)}
                   {::i18n/current-locale (comp/get-query i18n/Locale)}
                   {:normal-child (comp/get-query OtherChild)}]

   :initial-state (fn [& _]
                    {::i18n/current-locale (comp/get-initial-state i18n/Locale {:locale       :es
                                                                                :translations built-in-translations})
                     :hook                 (comp/get-initial-state Hook {:id 1 :child-id 100})
                     :hooks                [(comp/get-initial-state Hook {:id 2 :child-id 101})
                                            (comp/get-initial-state Hook {:id 3 :child-id 102})]
                     :normal-child         (comp/get-initial-state OtherChild {:id 1000 :n 100})})}
  (dom/div
    (dom/button {:onClick (fn [] (comp/transact! this [(i18n/change-locale {:locale :en})]))} "EN")
    (dom/button {:onClick (fn [] (comp/transact! this [(i18n/change-locale {:locale :es})]))} "ES")
    (dom/h2 (tr "A"))
    (ui-other-child normal-child)
    (dom/h2 (tr "B"))
    (ui-hook hook)
    (dom/h2 (tr "Children"))
    (mapv ui-hook hooks)))

(ws/defcard hook-demo-card
  (ct.fulcro/fulcro-card
    {::ct.fulcro/wrap-root? false
     ::ct.fulcro/root       Root
     ::ct.fulcro/app        (merge (react18-options)
                              {:shared    {::i18n/message-formatter icu/format}
                               :shared-fn ::i18n/current-locale})}))
