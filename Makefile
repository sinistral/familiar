
version  = $(shell cat VERSION)
docdir   = target/doc
srcfiles = $(shell find source -name *.cl*)
tstfiles = $(shell find test -name *.cl*)

.DEFAULT: jar

.PHONY: test
test: $(tstfiles)
	boot -- test-all

.PHONY: jar
jar: target/familiar-$(version).jar

target/familiar-$(version).jar: $(srcfiles)
	boot -- test-all -- build-jar -- target

.PHONY: install
install: jar
	boot -- install-jar

.PHONY: clean
clean:
	rm -rf target

.PHONY: doc
doc:
	mkdir -p target
	rm -rf $(docdir) && mkdir $(docdir)
	git clone git@github.com:sinistral/familiar.git $(docdir)
	cd $(docdir) \
		&& git symbolic-ref HEAD refs/heads/gh-pages \
		&& rm .git/index \
		&& git clean -fdx \
		&& git checkout gh-pages
	boot -- build-doc -- target --no-clean
	cd $(docdir) \
		&& git add . \
		&& git commit -m "Update API docs: $(version)" \
		&& git push -u origin gh-pages
