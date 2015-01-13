package com.ztomic.ircbot.listener;

import com.ztomic.ircbot.model.User.Level;

public interface Command {

	public String getName();
	public Level getLevel();
	
}
