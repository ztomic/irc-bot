package com.ztomic.ircbot.listener;

import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;

public interface Command {

	String getName();

	Level getLevel();

	default boolean isAllowed(User user) {
		return getLevel() == null || user.getLevel().ordinal() >= getLevel().ordinal();
	}

	static Command create(String name, Level level) {
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
				return getName();
			}
		};
	}
	
}
