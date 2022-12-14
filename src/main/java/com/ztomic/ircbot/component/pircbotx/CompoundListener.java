package com.ztomic.ircbot.component.pircbotx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.pircbotx.Utils;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;

public class CompoundListener implements Listener {
	
	private final List<Listener> listeners = new ArrayList<>();
	
	public CompoundListener(Set<Listener> listeners) {
		this.listeners.addAll(listeners);
		AnnotationAwareOrderComparator.sort(this.listeners);
	}

	@Override
	public void onEvent(Event event) throws Exception {
		Utils.addBotToMDC(event.getBot());
		for (Listener listener : listeners) {
			listener.onEvent(event);
		}
	}

}
