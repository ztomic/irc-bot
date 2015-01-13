package com.ztomic.ircbot.listener;

import static com.ztomic.ircbot.configuration.Formats.LISTENER_AVAILABLE_COMMANDS_FORMAT;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ztomic.ircbot.configuration.Formats;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.util.Util;

@Component
public class HelpListener extends CommandListener {
	
	@Autowired
	private List<CommandListener> listeners;
	
	@Override
	public String getName() {
		return "Help";
	}
	
	@Override
	public Set<? extends Command> getCommands() {
		return Collections.singleton(createCommand("HELP", Level.NEWBIE));
	}

	@Override
	public void handleCommand(GenericMessageEvent<PircBotX> event, Command command, User user, String[] arguments) {
		boolean foundSome = false;
		for (CommandListener listener : listeners) {
			Set<? extends Command> handlerCommands = listener.getCommands(user);
			if (handlerCommands != null && !handlerCommands.isEmpty()) {
				foundSome = true;
				event.getBot().sendIRC().message(user.getNick(), String.format(LISTENER_AVAILABLE_COMMANDS_FORMAT, listener.getName(), user.getLevel(), listener.getCommandPrefix(), Util.formatCollection(handlerCommands, ", ")));
			}
		}
		
		if (!foundSome) {
			event.getBot().sendIRC().message(user.getNick(), String.format(Formats.NO_AVAILABLE_COMMANDS_FORMAT, user.getLevel()));
		}
	}
}
