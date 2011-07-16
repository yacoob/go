NAME = go-runner
INITDIR = /etc/init.d
CFGDIR = /etc/default
CONFIGFILE = configfile
INITSCRIPT = initscript

all: install

install: conf init

conf: configfile
	cp -f $(CONFIGFILE) $(CFGDIR)/$(NAME)
	chmod a+r $(CFGDIR)/$(NAME)

init: initscript
	cp -f $(INITSCRIPT) $(INITDIR)/$(NAME)
	chmod a+rx $(INITDIR)/$(NAME)
	update-rc.d $(NAME) defaults
