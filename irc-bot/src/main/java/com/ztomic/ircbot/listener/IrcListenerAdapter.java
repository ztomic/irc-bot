package com.ztomic.ircbot.listener;

import java.util.concurrent.Semaphore;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericUserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration.QuizMessages;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.repository.UserRepository;

@EnableConfigurationProperties({IrcConfiguration.class, MessagesConfiguration.class})
public class IrcListenerAdapter extends ListenerAdapter<PircBotX> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected static final String COMMAND_PREFIX = "-";
	
	private static Semaphore SEMAPHORE = new Semaphore(1);
	
	@Autowired
	protected IrcConfiguration ircConfiguration;
	
	@Autowired
	protected MessagesConfiguration messagesConfiguration;
	
	@Autowired
	protected UserRepository userRepository;
	
	public String getName() {
		return getClass().getSimpleName();
	}

	public User getUser(GenericUserEvent<PircBotX> event) {
		try {
			try {
				SEMAPHORE.acquire();
			} catch (InterruptedException e) {
				//
			}
			User user = null;
			try {
				user = userRepository.findByServerAndNick(event.getBot().getConfiguration().getServerHostname(), event.getUser().getNick());
				if (user == null) {
					user = new User();
					user.setNick(event.getUser().getNick());
					user.setHostname(event.getUser().getHostmask());
					user.setIdent(event.getUser().getLogin());
					user.setServer(event.getBot().getConfiguration().getServerHostname());
					user = userRepository.saveAndFlush(user);
				}
			} catch (Throwable t) {
				log.error("Error adding new user: " + event, t);
			}
			return user;
		} finally {
			SEMAPHORE.release();
		}
	}
	
	public QuizMessages getQuizMessages(String language) {
		return messagesConfiguration.getQuizMessages(language);
	}
	
	public QuizMessages getQuizMessages() {
		return getQuizMessages(null);
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getClass().getName() + ")";
	}
	
}
