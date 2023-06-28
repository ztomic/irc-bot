package com.ztomic.ircbot.listener;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class ErrorListener extends ListenerAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void onException(ExceptionEvent event) {
		log.error(event.getMessage(), event.getException());
	}
}
