package com.ztomic.ircbot.component.pircbotx;

import java.util.concurrent.ExecutorService;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener manager koji u odnosu na {@link ThreadedListenerManager} koristi jedan
 * thread po eventu.
 */
public class CustomThreadedListenerManager extends ThreadedListenerManager<PircBotX> {
	
	protected static final Logger log = LoggerFactory.getLogger(CustomThreadedListenerManager.class);

	public CustomThreadedListenerManager() {
		super();
	}
	
	public CustomThreadedListenerManager(ExecutorService pool) {
		super(pool);
	}
	
	@Override
	public void dispatchEvent(Event<PircBotX> event) {
		log.trace("dispatchEvent({})", event);
		ListenerList listeners = new ListenerList(getListenersReal());
		submitEvent(pool, listeners, event);
	}
	
}
