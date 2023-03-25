(ns com.fulcrologic.fulcro-i18n.i18n-sample-locale-selector
  (:require
    [clojure.string :as str]
    [com.fulcrologic.fulcro-i18n.i18n :as i18n]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:cljs [com.fulcrologic.fulcro.dom :as dom]
       :clj  [com.fulcrologic.fulcro.dom-server :as dom])))

(defsc LocaleSelector
  "A reusable locale selector. Generates a simple `dom/select` with CSS class fulcro$i18n$locale_selector.

  Remember that for localization to work you *must* query for `::i18n/current-locale` in your root
  component with the query [{::i18n/current-locale (prim/get-query Locale)}]."
  [this {:keys [::i18n/available-locales ::i18n/current-locale]}]
  {:query         [{::i18n/available-locales (comp/get-query i18n/Locale)}
                   {[::i18n/current-locale '_] (comp/get-query i18n/Locale)}]
   :initial-state {::i18n/available-locales :param/locales}}
  (let [{::i18n/keys [locale]} current-locale
        locale-kw (fn [l] (-> l (str/replace #":" "") keyword))]
    (dom/select :.fulcro$i18n$locale_selector
      {:onChange (fn [evt] #?(:cljs (comp/transact! this
                                      [(i18n/change-locale {:locale (locale-kw (evt/target-value evt))})])))
       :value    locale}
      (map-indexed
        (fn [i {::i18n/keys [locale] :ui/keys [locale-name]}]
          (dom/option {:key i :value locale} locale-name))
        available-locales))))

(def ui-locale-selector (comp/factory LocaleSelector))
