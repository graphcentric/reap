;; Copyright © 2024, JUXT LTD.

(ns juxt.reap.rfc6570-test
  (:require
   [clojure.test :refer [deftest is are testing]]
   [juxt.reap.decoders.rfc6570 :refer [match-uri compile-uri-template]]))

(deftest match-uri-test
  (are [uri-template uri expected]
   (=
    expected
    (:vars
     (match-uri
      (compile-uri-template uri-template)
      uri)))

    "http://example.com/~{username}/"
    "http://example.com/~mal/"
    {"username" "mal"}

    "http://example.com/~{username,id}/"
    "http://example.com/~mal,01/"
    {"username" "mal" "id" "01"}

    "http://example.com/dictionary/{term:1}/{term}"
    "http://example.com/dictionary/c/cat"
    {"term" "cat"}

    "http://example.com/search{?q,lang}"
    "http://example.com/search?q=chien&lang=fr"
    {"q" "chien", "lang" "fr"}

    "http://example.com/file{.suffix}"
    "http://example.com/file.svg"
    {"suffix" "svg"}

    ;; Level 3

    "X{.var}"
    "X.value"
    {"var" "value"}

    "X{.x,y}"
    "X.1024.768"
    {"x" "1024" "y" "768"}

    ;; Level 4

    "X{.list}"
    "X.red,green,blue"
    {"list" ["red" "green" "blue"]}

    "X{.list*}"
    "X.red.green.blue"
    {"list" ["red" "green" "blue"]}

    ))


#_(let [uri-template "X{.list}"
      uri "X.red,green,blue"]
  (match-uri
   (compile-uri-template uri-template)
     uri))
