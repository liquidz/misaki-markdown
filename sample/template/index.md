; @layout default
; @title  misaki markdown

# [$(site-title)]($(root))

[Misaki](https://github.com/liquidz/misaki) markdown compiler.
[Jekyll](https://github.com/mojombo/jekyll) inspired static site generator in Clojure.

## Sample posts

@(for posts)
* [$(date-format date "yyyy MM dd") - $(title)]($(url))
@(end)

@(if next-page)
[Next page &raquo;]($(next-page))
@(end)

@(if prev-page)
[&laquo; Prev page]($(prev-page))
@(end)

## Custom function

* \_config.clj
```
:cuma { :extension "extension.clj" }
```
* template
```
$(hello "world")
```
* output
$(hello "world")


## Template source
```
; @layout default
; @title  misaki markdown

# [$(site-title)]($(root))

[Misaki](https://github.com/liquidz/misaki) markdown compiler.
[Jekyll](https://github.com/mojombo/jekyll) inspired static site generator in Clojure.

## Sample posts

@(for posts)
* [$(date) - $(title)]($(url))
@(end)

@(if next-page)
[Next page &raquo;]($(next-page))
@(end)

@(if prev-page)
[&laquo; Prev page]($(prev-page))
@(end)

## Custom function

* _config.clj
* template
* output

## Template source

## Document
Please wait.
```

## Document

See <https://github.com/liquidz/misaki-markdown>.

