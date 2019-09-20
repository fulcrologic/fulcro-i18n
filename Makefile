tests:
	npm install
	npx shadow-cljs compile ci-tests
	npx karma start --single-run
	clojure -A:dev:test:clj-tests

# gem install asciidoctor asciidoctor-diagram
# gem install coderay
I18N.html: I18N.adoc
	asciidoctor -o I18N.html -b html5 -r asciidoctor-diagram I18N.adoc

book: I18N.html

dev:
	clojure -A:dev:provided:test:clj-tests --watch --fail-fast 
