package com.ztomic.ircbot.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.repository.UserRepository;
import com.ztomic.ircbot.util.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.util.StringUtils;

public abstract class CommandListener extends IrcListenerAdapter {

	public CommandListener(IrcConfiguration ircConfiguration, MessagesConfiguration messagesConfiguration, UserRepository userRepository) {
		super(ircConfiguration, messagesConfiguration, userRepository);
	}

	@Override
	public void onGenericMessage(GenericMessageEvent event) {
		if (isCommand(event.getMessage())) {
			String[] tokens = tokenize(event.getMessage());
			User user = getUser(event);
			Command command = getCommand(user, tokens);
			if (command != null) {
				String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
				log.debug("User [" + user + "] is calling command [" + command + "] with arguments " + Arrays.toString(args) + "");
				if (!isAllowed(command, user)) {
					event.respond(Colors.paintString(Colors.RED, "You're not allowed to execute command: " + command + ", required level is: " + command.getLevel() + ", you have: " + user.getLevel()));
					return;
				}
				handleCommand(event, command, user, args);
			}
		} else {
			handleMessage(event);
		}
	}

	public boolean isCommand(String message) {
		return StringUtils.hasText(message) && message.startsWith(getCommandPrefix()) && message.length() > getCommandPrefix().length();
	}
	
	public Set<? extends Command> getCommands() {
		return Collections.emptySet();
	}
	
	public Set<? extends Command> getCommands(User user) {
		return getCommands().stream().filter(c -> isAllowed(c, user)).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public String getCommandPrefix() {
		return COMMAND_PREFIX;
	}
	
	public String extractCommandPrefix(String message) {
		if (StringUtils.hasText(message)) {
			if (message.startsWith(getCommandPrefix())) {
				return message.substring(getCommandPrefix().length());
			}
		}
		return message;
	}
	
	public String[] tokenize(String message) {
		if (StringUtils.hasLength(message)) {
			return message.split(" ");
		} else {
			return new String[]{message};
		}
	}
	
	public Command getCommand(User user, String[] tokens) {
		if (tokens != null && tokens.length > 0) {
			String commandName = extractCommandPrefix(tokens[0]);
			for (Command c : getCommands()) {
				if (c.getName().equalsIgnoreCase(commandName)) {
					return c;
				}
			}
		}
		return null;
	}
	
	public boolean isAllowed(Command command, User user) {
		return user.getLevel().ordinal() >= command.getLevel().ordinal();
	}
	
	public Command createCommand(final String name, final Level level) {
		return new Command() {
			
			@Override
			public String getName() {
				return name;
			}
			
			@Override
			public Level getLevel() {
				return level;
			}
			
			@Override
			public String toString() {
				return name;
			}
		};
	}

	public abstract void handleCommand(GenericMessageEvent event, Command command, User user, String[] arguments);
	
	public void handleMessage(GenericMessageEvent event) {
		
	}
}
