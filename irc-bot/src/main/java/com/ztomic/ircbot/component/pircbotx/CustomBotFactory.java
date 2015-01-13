package com.ztomic.ircbot.component.pircbotx;

import org.pircbotx.Configuration.BotFactory;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

public class CustomBotFactory extends BotFactory {
	
	private double messageLimit;
	private double messageLimitInterval;
	
	public CustomBotFactory(double messageLimit, double messageLimitInterval) {
		this.messageLimit = messageLimit;
		this.messageLimitInterval = messageLimitInterval;
	}
	
	@Override
	public OutputRaw createOutputRaw(PircBotX bot) {
		return new CustomOutputRaw(bot, messageLimit, messageLimitInterval);
	}
	
}
