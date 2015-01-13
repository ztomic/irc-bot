package com.ztomic.ircbot.listener;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.QuitEvent;
import org.springframework.stereotype.Component;

import com.ztomic.ircbot.configuration.IrcConfiguration;

@Component
public class KeepNickListener extends IrcListenerAdapter {
	
	@Override
	public String getName() {
		return "NickKeeper";
	}

	@Override
	public void onQuit(QuitEvent<PircBotX> event) throws Exception {
		IrcConfiguration.ServerConfig config = ircConfiguration.getServer(event.getBot().getConfiguration().getServerHostname());
		if (config != null) {
			if (config.isAutoNickChange()) {
				if (event.getUser().getNick().equals(config.getName()) && !event.getBot().getUserBot().getNick().equals(config.getName())) {
					log.debug("Changing bot nick from {} to configured {}", event.getBot().getNick(), config.getName());
					event.getBot().sendIRC().changeNick(config.getName());
				}
			}
		}
	}
}
