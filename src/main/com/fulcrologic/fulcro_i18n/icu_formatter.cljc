(ns com.fulcrologic.fulcro-i18n.icu-formatter
  (:require
    [com.fulcrologic.fulcro-i18n.i18n :as i18n]
    #?(:cljs ["intl-messageformat" :default IntlMessageFormat]))
  #?(:clj (:import (com.ibm.icu.text MessageFormat)
                   (java.util Date Locale))))

(defn format
  "Format a string using Intl MessageFormat (cljs https://formatjs.io/docs/intl-messageformat/) or
   IBM ICU (clj https://unicode-org.github.io/icu-docs/apidoc/dev/icu4j/com/ibm/icu/text/MessageFormat.html).

   These two implement the same standard and do a fair job of CLJC functionality.

   See the docs on either of those projects for a description of the strings you should use when
   formatting data.
   "
  [{:keys [::i18n/localized-format-string
           ::i18n/locale ::i18n/format-options]}]
  #?(:cljs
     (try
       (let [locale-str (name locale)
             formatter  (IntlMessageFormat. localized-format-string locale-str)]
         (.format formatter (clj->js format-options)))
       (catch :default e (str "??? " (ex-message e))))
     :clj
     (let [locale-str (name locale)]
       (try
         (let [formatter (new MessageFormat localized-format-string (Locale/forLanguageTag locale-str))]
           (.format formatter format-options))
         (catch Throwable e (str "??? " (ex-message e)))))))
