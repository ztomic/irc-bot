package com.ztomic.ircbot.listener;

import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.repository.UserRepository;
import org.pircbotx.hooks.events.QuitEvent;
import org.springframework.stereotype.Component;

@Component
public class KeepNickListener extends IrcListenerAdapter {

	public KeepNickListener(IrcConfiguration ircConfiguration, MessagesConfiguration messagesConfiguration, UserRepository userRepository) {
		super(ircConfiguration, messagesConfiguration, userRepository);
	}

	@Override
	public String getName() {
		return "NickKeeper";
	}

	@Override
	public void onQuit(QuitEvent event) {
		IrcConfiguration.ServerConfig config = ircConfiguration.getServer(event.getBot().getServerHostname());
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
