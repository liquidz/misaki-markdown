![misaki markdown logo](https://raw.github.com/liquidz/misaki-markdown/master/sample/public/img/logo.png)

[![Build Status](https://secure.travis-ci.org/liquidz/misaki-markdown.png)](http://travis-ci.org/liquidz/misaki-markdown)

misaki-markdown is one of [misaki](http://liquidz.github.io/misaki/)'s compiler plugin.
This compiler allows you to use misaki Markdown templates instead of S-exp templates.

## Example template

```
; @layout default
; @title  sample title

# $(title)

welcome to misaki-markdown's sample page.

## posts

@(for posts)
 * [$(date-format date "yyyy MM dd") - $(title)]($(url))
@(end)

## paging

@(if next-page)
[Next page &raquo;]($(next-page))
@(end)

@(if prev-page)
[&laquo; Prev page]($(prev-page))
@(end)
```

## Usage

### Run sample

```sh
$ git clone git@github.com:liquidz/misaki-markdown.git
$ cd misaki-markdown
$ lein run sample
```

## Configuration

### Code detection
```clj
;_config.clj

;; code regexp setting
;;   default value: #"(?s)```([^\r\n]*)[\r\n]+(.+?)[\r\n]+```"
:code-regexp #"(?s)```([^\r\n]*)[\r\n]+(.+?)[\r\n]+```"
```
### Output code html
```clj
;_config.clj

;; code html format setting
;;   defailt value; "<pre><code@(if lang) class="brush: $(lang);"@(end)>$(code)</code></pre>"
:code-html-format "<pre><code@(if lang) class=\"brush: $(lang);\"@(end)>$(code)</code></pre>"
```

## License

Copyright (C) 2013 Masashi Iizuka([@uochan](http://twitter.com/uochan/))

Distributed under the Eclipse Public License, the same as Clojure.
