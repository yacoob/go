NAME = go-runner
INITDIR = /etc/init.d
CFGDIR = /etc/default

all: install

install: conf init

conf: configfile
	cp -f configfile $(CFGDIR)/$(NAME)
	chmod a+r $(CFGDIR)/$(NAME)

init: initscript
	cp -f initscript $(INITDIR)$(NAME)
	chmod a+rx $(INITDIR)/$(NAME)
	update-rc.d $(NAME) defaults
