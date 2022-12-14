package com.ztomic.ircbot.listener;

import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration.QuizMessages;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.repository.UserRepository;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericUserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrcListenerAdapter extends ListenerAdapter {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected static final String COMMAND_PREFIX = "-";
	
	private static final Semaphore SEMAPHORE = new Semaphore(1);

	protected final IrcConfiguration ircConfiguration;
	protected final MessagesConfiguration messagesConfiguration;
	protected final UserRepository userRepository;

	public IrcListenerAdapter(IrcConfiguration ircConfiguration, MessagesConfiguration messagesConfiguration, UserRepository userRepository) {
		this.ircConfiguration = ircConfiguration;
		this.messagesConfiguration = messagesConfiguration;
		this.userRepository = userRepository;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	public User getUser(GenericUserEvent event) {
		try {
			try {
				SEMAPHORE.acquire();
			} catch (InterruptedException e) {
				//
			}
			User user = null;
			try {
				user = userRepository.findByServerAndNickIgnoreCase(event.getBot().getServerHostname(), event.getUser().getNick());
				if (user == null) {
					user = new User();
					user.setNick(event.getUser().getNick());
					user.setHostname(event.getUser().getHostmask());
					user.setIdent(event.getUser().getLogin());
					user.setServer(event.getBot().getServerHostname());
					user = userRepository.saveAndFlush(user);
				}
			} catch (Throwable t) {
				log.error("Error adding new user: {}", event, t);
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
	
	public static String formatCollection(Collection<?> col, String separator) {
		if (col == null) {
			return "";
		}
		return col.stream().map(c -> c != null ? c.toString() : null).collect(Collectors.joining(separator));
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getClass().getName() + ")";
	}
	
}
