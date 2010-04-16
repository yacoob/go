# This is an url redirector.
>With a little twist, packaged into a [bottle](http://bottle.paws.de/).

## In order to run:
* get source
* `easy_install -U bottle`
* `./go.py`

**WARNING:** it doesn't have any kind of ACLs, and it would be probably most
unwise to set this thing on a publicly available machine. It's a small gizmo
written for my home network, when users are either trusted, or whackable with
a pointy stick.

## There's normal part
...which is URL redirector.

* `/and/list` or `/` - list of current shortcuts;
* `/foo*` - edit shortcut `foo`.

It doesn't do random shortcuts, because I didn't need those. I wanted short,
human-readable urls, like `go/bank`. And for those occasions that you just want
to send a long URL to another person, there's this crazy part.

## Crazy part
...is called
[Trampoline](http://swissarmyhammer.wordpress.com/2009/05/17/sproing/). Go
ahead, read it, and scowl in horror.

In short, it's a LIFO stack of URLs, that
you can use in order to bounce an URL from one computer to the other. You push
to stack from the first one, and you pop from the stack on the other one.

* `/hop/list` - current stack content ("New URLs") and recently exchanged URLs ("Old URLs");
* `/hop/pop` - go to last pushed URL;
* `/hop/push` - push a new URL (via `?url=...` parameter).
* `/hop/rss` - feed of new URLs

`chrome` directory contains simple Chrome addon, that allows you to
push/pop/interact with Trampolina part of go without fiddling with URLs on your
own. Remember to configure it properly before using. For the time being, it's
not available in Chrome Extension Gallery, and you have to install it by hand.
