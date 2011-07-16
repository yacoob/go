_This is the blog entry that originaly accompanied very first version of what is nowadays Trampoline module for go._

# Trampolina,
> aka solution to the problem that only I had.

Trampolina was written to solve a problem of mine: two people, two computers on
the same network. One of those people find interesting article on the web, and
wants to share it with the other person. Now, any sane person would just use
email or IM to send the URL over. That was the case for me as well, but I’ve
noticed that every time I need to launch GMail, or Adium — they were never
around when needed. Of course I could have just train myself to have them
always open, but I wanted to write some small and funny Python webapp... and I
did.

This “small and funny” application is Trampolina. Small web application that
serves only one purpose — LIFO stack of URLs. Here’s how Alice and Bob, our
favorite IT demonstrators can use it:

* Trampolina is installed on
`http://home-nas:9099/`
* Alice and Bob are sitting in front of their computers, next to each other, browsing the Web furiously.
* Alice finds amazing article in Wikipedia:
`http://en.wikipedia.org/wiki/African_swallow`
* Alice visits:
`http://home-nas:9099/push?url=http://en.wikipedia.org/wiki/African_swallow`
As it would be rather peculiar to remember such complicated URL, Alice uses special bookmarklet for that purpose.
* Alice pokes Bob who sits next to her.
* Bob goes to:
`http://home-nas:9099/pop`
* Bob ends up knowing way too much about African swallows.

If either Alice or Bob wants to revisit some URL, backlog of them can be browsed at:

    http://home-nas:9099/list

If the distance between Alice and Bob doesn’t allow for convenient poking, there’s an RSS that B can follow:

    http://home-nas:9099/rss

# Beware
Please note, that the fact that there’s an RSS available does not suggest in
any way that you should run Trampolina in the wild wild Internet. Think of this
RSS as a way of exporting URLs in a convenient format, so you can plug it into
some third party software (ie. screensaver, or ticker).

Trampolina doesn’t provide fabulous interface, nor any authentication nor
authorisation. It should be only used in secure environment, when you can trust
the clients. In most cases, this is your home network, with few computers. If
you start asking questions along the lines of "How do I make sure people won’t
spam me with that", then you’re obviously trying to use Trampolina in wrong
place.

# Installation
Whole thing lives in single Python script, trampolina.py. It doesn’t require any extra libs apart from Python itself. If you launch it without any arguments:

    $ ./trampolina.py

* it will use database located in `/tmp/db`;
* it will listen on all interfaces, on port 9099
* If you don’t like those defaults, provide Trampolina with arguments:

    $ ./trampolina.py localhost 12345 /var/tmp/trampolina-database

To prepare database for Trampolina, run:

    $ make initdb

and copy resulting db file to location of your choice.

`bookmarklet.js` contains a bookmarklet that you can use with your browser for
quick sending of URLs to Trampolina. Just add a bookmark that has content of
`bookmarklet.js` in place of destination address. You’ll most likely need to edit
the address that is used in there, to match the address of your Trampolina
instance. Once you do that, every time you click on that bookmark, you’ll send
URL of the currently viewed page to Trampolina. For extra laziness, you can get
some third party software (like Spark for Mac OSX, or any decent window manager
for Linux) that will use that bookmarklet for you once you press defined key
combination.

`trampolina.sh` can be used as `/etc/init.d/` script for launching Trampolina
on boot. It’s deliberately very simple (eg. no start-stop-daemon) to make sure
it’ll work in most of the systems.

# FAQ

**Q:** Do you know that normal people just send URLs with IM or email?

**A:** Yes. Apparently, I’m an outlier. The fact that I always had to bring
up GMail/Jabber client, and lookup the person that sits next to me was the very
thing that prompted me to write Trampolina. Of course the other person also had
to have her client up and running — and most of the time she didn’t. And I
wanted to write something small and funny in Python anyway :) So if you’re
unlike me, and are happy with the way you send urls around, Trampolina is not
for you.

**Q:** Can you add feature X to Trampolina?

**A:** I probably can. I also most likely won’t, as I wrote Trampolina to solve
specific problem I had. It got written, it’s doing its job, I’m very happy with
its behavior. I really don’t see any need to add features to that thing.

**Q:** I’d really like to use Trampolina on the Internet!

**A:** Don’t. It wasn’t designed for that. If you want to share URLs with
others online, there are plenty of services that allow you to do that much
better (del.icio.us, reddit, digg, etc.). And if you’re stubborn and use it
despite my warnings... well, good luck.

**Q:** The source code! It’s ugly! My eyes!

**A:** I know. Sorry about that. The inline “templates” and the fact that the
classes are not split into separate files were intentional. I wanted to make
the application as “compact” as possible, without need to set up directory
structure and whatnot. The script is also suitable for `cx_freeze` treatment.

**Q:** What’s the deal with the strange name?

**A:** It’s “trampoline” in Polish. Trampoline, because if you set it up
between two computers, you can bounce urls from one to another. Cartooney sound
effect not included.
