# This is an url redirector.
>With a little twist, packaged into a [bottle](http://bottlepy.org).

## In order to run:
* `cd python`
* `python setup.py install`

Some degree of `sudo` intervention might be required in the above, depending on
your setup. Remember to also change your `umask` to something sensible (like
`022`), to avoid having installed files being unreadable to other users on the
system.

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
...is called Trampoline. See `trampoline-story.md` for the story behind it. Go
ahead, read it, and scowl in horror.

In short, it's a LIFO stack of URLs, that
you can use in order to bounce an URL from one computer to the other. You push
to stack from the first one, and you pop from the stack on the other one.

* `/hop/list` - current stack content ("New URLs") and recently exchanged URLs ("Old URLs");
* `/hop/pop` - go to last pushed URL;
* `/hop/push` - push a new URL (via `?url=...` parameter).
* `/hop/rss` - feed of new URLs

`/hop/r/` contains rudimentary REST interface for Trampoline module.

## Extra bits
`android` directory contains simple Android client for Trampoline module.

`chrome` directory contains simple Chrome addon, that allows you to
push/pop/interact with Trampoline part of go without fiddling with URLs on your
own. Remember to configure it properly before using. For the time being, it's
not available in Chrome Extension Gallery, and you have to install it by hand.

`init` directory contains an `init.d` script, based on Debian's `skeleton`
example. If you're installing Go on a Debian-flavoured machine, it might be
sufficient to say `make`. YMMV, though.
