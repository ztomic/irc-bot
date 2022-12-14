package com.ztomic.ircbot.component.pircbotx;

import java.util.concurrent.ExecutorService;

import org.pircbotx.Utils;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ThreadedListenerManager;

/**
 * Listener manager koji u odnosu na {@link ThreadedListenerManager} koristi jedan
 * thread po eventu.
 */
public class CustomThreadedListenerManager extends ThreadedListenerManager {
	
	public CustomThreadedListenerManager(ExecutorService pool) {
		super(pool);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event.getBot() != null) Utils.addBotToMDC(event.getBot());
		CompoundListener listeners = new CompoundListener(getListenersReal());
		submitEvent(pool, listeners, event);
	}
	
}
