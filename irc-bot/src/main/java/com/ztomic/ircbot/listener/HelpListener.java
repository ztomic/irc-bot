package com.ztomic.ircbot.listener;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ztomic.ircbot.configuration.Formats;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;

@Component
public class HelpListener extends CommandListener {
	
	private final List<CommandListener> listeners;

	@Autowired
	public HelpListener(List<CommandListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public String getName() {
		return "Help";
	}
	
	@Override
	public Set<? extends Command> getCommands() {
		return Collections.singleton(createCommand("HELP", Level.NEWBIE));
	}

	@Override
	public void handleCommand(GenericMessageEvent event, Command command, User user, String[] arguments) {
		boolean foundSome = false;
		Formats formats = getQuizMessages().getFormats();
		for (CommandListener listener : listeners) {
			Set<? extends Command> handlerCommands = listener.getCommands(user);
			if (handlerCommands != null && !handlerCommands.isEmpty()) {
				foundSome = true;
				event.getBot().sendIRC().message(user.getNick(), String.format(formats.getListenerAvailableCommandsFormat(), listener.getName(), user.getLevel(), listener.getCommandPrefix(), formatCollection(handlerCommands, ", ")));
			}
		}
		
		if (!foundSome) {
			event.getBot().sendIRC().message(user.getNick(), String.format(formats.getNoAvailableCommandsFormat(), user.getLevel()));
		}
	}
}
