package com.ztomic.ircbot.component.pircbotx;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class CustomPircBotX extends PircBotX {

	public CustomPircBotX(Configuration<? extends PircBotX> configuration) {
		super(configuration);
	}
	
	public boolean isReconnectStopped() {
		return reconnectStopped;
	}

}
