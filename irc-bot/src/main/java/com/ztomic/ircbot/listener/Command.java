package com.ztomic.ircbot.listener;

import com.ztomic.ircbot.model.User.Level;

public interface Command {

	String getName();
	Level getLevel();
	
}
