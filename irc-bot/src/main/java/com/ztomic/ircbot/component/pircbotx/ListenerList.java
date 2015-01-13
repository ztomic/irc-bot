package com.ztomic.ircbot.component.pircbotx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class ListenerList implements Listener<PircBotX> {
	
	private List<Listener<PircBotX>> listeners = new ArrayList<>();
	
	public ListenerList(Set<Listener<PircBotX>> listeners) {
		this.listeners.addAll(listeners);
		AnnotationAwareOrderComparator.sort(this.listeners);
	}

	@Override
	public void onEvent(Event<PircBotX> event) throws Exception {
		for (Listener<PircBotX> listener : listeners) {
			listener.onEvent(event);
		}
	}

}
