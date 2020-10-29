;; Copyright © 2020, JUXT LTD.

(ns juxt.reap.alpha.rfc5646
  (:require
   [clojure.string :as str]
   [juxt.reap.alpha :as reap]
   [juxt.reap.alpha.combinators :as p]))

(defn- langtag [_]
  (p/into
   {::lang-type :langtag}
   (p/pattern-parser
    (re-pattern
     (str
      "(?<Language>"
      "(?<LangCode>\\p{Alpha}{2,3})"
      "(?!\\p{Alpha})"
      "(?:-(?<ExtLang>\\p{Alpha}{3}(?:-\\p{Alpha}{3}){0,2}))?"
      "(?!\\p{Alpha})"
      "|"
      "(?<Reserved>\\p{Alpha}{4})(?!\\p{Alpha})"
      "|"
      "(?<RegLangSubtag>\\p{Alpha}{5,8})(?!\\p{Alpha})"
      ")"

      "(?:\\-(?<Script>\\p{Alpha}{4}))?"
      "(?!\\p{Alpha})"

      "(?:\\-(?<Region>(\\p{Alpha}{2}|\\p{Digit}{3})))?"
      "(?!\\p{Alnum})"

      "(?:\\-(?<Variant>\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}))*"
      "(?!\\p{Alnum})"

      "(?:\\-(?<Extension>[0-9A-WY-Za-wy-z](?:-\\p{Alnum}{2,8})+))*"
      "(?!\\p{Alnum})"

      "(?:\\-(?<PrivateUse>x-(?<PrivateTag>\\p{Alnum}{1,8}(?:-\\p{Alnum}{1,8})*)))?"
      ;; Negative lookahead on subsequent hyphen
      "(?!\\p{Alnum}|-)"))

    {:group
     #::{:langtag 0
         :language "Language"
         :code "LangCode"
         :extlang "ExtLang"
         :reserved "Reserved"
         :reg-lang-subtag "RegLangSubtag"
         :script "Script"
         :region "Region"
         :variant "Variant"
         :extension "Extension"
         :privateuse "PrivateUse"
         :privatetag "PrivateTag"}})))

(defn- privateuse [_]
  (p/into
   {::lang-type :privateuse}
   (p/pattern-parser
    #"x-(\p{Alnum}{1,8}(?:-\p{Alnum}{1,8})*)(?!\p{Alnum})"
    {:group
     {::langtag 0
      ::privatetag 1}})))

(defn- match-literal
  ([set]
   (match-literal set {}))
  ([set opts]
   (p/pattern-parser
    (re-pattern
     (str/join
      "|"
      (map
       (fn [s]
         (format
          (str
           "(?i:\\Q%s\\E)"
           ;; We add some negative lookahead to avoid matching prefixes
           "(?!\\p{Alpha})") s))
       set)))
    opts)))

(defn- irregular [opts]
  (p/into
   {::lang-subtype :irregular}
   (match-literal
    #{"en-GB-oed"
      "i-ami"
      "i-bnn"
      "i-default"
      "i-enochian"
      "i-hak"
      "i-klingon"
      "i-lux"
      "i-mingo"
      "i-navajo"
      "i-pwn"
      "i-tao"
      "i-tay"
      "i-tsu"
      "sgn-BE-FR"
      "sgn-BE-NL"
      "sgn-CH-DE"}
    (conj opts [:group {::langtag 0}]))))

(defn- regular [opts]
  (p/into
   {::lang-subtype :regular}
   (match-literal
    #{"art-lojban"
      "cel-gaulish"
      "no-bok"
      "no-nyn"
      "zh-guoyu"
      "zh-hakka"
      "zh-min"
      "zh-min-nan"
      "zh-xiang"}
    (conj opts [:group {::langtag 0}]))))

(defn- grandfathered [opts]
  (p/into
   {::lang-type :grandfathered}
   (p/alternatives
    (irregular opts)
    (regular opts))))

(defn ^::reap/codec language-tag [opts]
  {::reap/decode
   (p/alternatives
    (langtag opts)
    (privateuse opts)
    (grandfathered opts))})
