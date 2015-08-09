package com.ztomic.ircbot.listener;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSortedSet;
import com.ztomic.ircbot.configuration.Formats;
import com.ztomic.ircbot.model.Seen;
import com.ztomic.ircbot.model.Seen.EventType;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.repository.SeenRepository;
import com.ztomic.ircbot.util.Colors;

@Component
public class SeenListener extends CommandListener {

	static final Pattern jbNickPattern = Pattern.compile("^([a-zA-Z0-9_-]+?)(?:\\||\\`).*$");
	static final Pattern irssiNickPattern = Pattern.compile("^_*([a-zA-Z0-9_-]+?)_*$");

	@Autowired
	private SeenRepository seenRepository;

	@Override
	public String getName() {
		return "Seen";
	}
	
	@Override
	public Set<? extends Command> getCommands() {
		return Collections.singleton(createCommand("SEEN", Level.REGISTERED));
	}
	
	@Override
	public void onEvent(Event<PircBotX> ev) throws Exception {
		super.onEvent(ev);
		PircBotX bot = ev.getBot();
		String server = bot.getConfiguration().getServerHostname();
		if (ev instanceof KickEvent) { 
			KickEvent<PircBotX> ck = (KickEvent<PircBotX>) ev;
			Seen s = findSeen(ck.getUser().getNick(), server);
			if (s == null) {
				s = new Seen();
			}
			org.pircbotx.User user = ck.getRecipient();
			s.setNick(user.getNick());
			s.setName(user.getRealName());
			s.setIdent(user.getLogin());
			s.setHost(user.getHostmask());
			s.setServer(server);
			s.setType(EventType.Kick);
			s.setChannel(ck.getChannel().getName());

			if (s.getDetail() != null) s.getDetail().clear();
			s.addDetail("kicked.from.nick", ck.getUser().getNick());
			s.addDetail("kicked.from.ident", ck.getUser().getLogin());
			s.addDetail("kicked.from.host", ck.getUser().getHostmask());
			s.addDetail("channel", ck.getChannel().getName());
			s.addDetail("kicked.reason", ck.getReason());
			s.setTime(new Date(ck.getTimestamp()));
			s = seenRepository.save(s);
		} else if (ev instanceof PartEvent) {
			PartEvent<PircBotX> cp = (PartEvent<PircBotX>) ev;
			Seen s = findSeen(cp.getUser().getNick(), server);
			if (s == null) {
				s = new Seen();
			}
			org.pircbotx.User user = cp.getUser();
			s.setNick(user.getNick());
			s.setName(user.getRealName());
			s.setIdent(user.getLogin());
			s.setHost(user.getHostmask());
			s.setServer(server);
			s.setType(EventType.Part);
			s.setChannel(cp.getChannel().getName());
			s.setTime(new Date(cp.getTimestamp()));
			if (s.getDetail() != null) s.getDetail().clear();
			s.addDetail("part.message", cp.getReason());
			s = seenRepository.save(s);
		} else if (ev instanceof JoinEvent) {
			
		} else if (ev instanceof QuitEvent) {
			QuitEvent<PircBotX> qe = (QuitEvent<PircBotX>) ev;
			Seen s = findSeen(qe.getUser().getNick(), server);
			if (s == null) {
				s = new Seen();
			}
			String channel = null;
			org.pircbotx.User user = qe.getUser();
			Set<String> channels = new HashSet<>();
			s.setNick(user.getNick());
			s.setName(user.getRealName());
			s.setIdent(user.getLogin());
			s.setHost(user.getHostmask());
			s.setServer(server);
			s.setType(EventType.Quit);
			s.setChannel(channel);
			s.setTime(new Date(qe.getTimestamp()));
			if (s.getDetail() != null) s.getDetail().clear();
			if (channels != null) s.addDetail("quit.channels", StringUtils.collectionToCommaDelimitedString(channels));
			s.addDetail("quit.message", qe.getReason());
			s = seenRepository.save(s);
		} else if (ev instanceof NickChangeEvent) {
			NickChangeEvent<PircBotX> nc = (NickChangeEvent<PircBotX>) ev;
			Seen s = findSeen(nc.getOldNick(), server);
			if (s == null) {
				s = new Seen();
			}
			org.pircbotx.User user = nc.getUser();
			Set<String> channels = new HashSet<>();
			for (Channel chan : user.getChannels()) {
				if (user != null) {
					channels.add(chan.getName());
				}
			}
			s.setNick(nc.getOldNick());
			s.setName(user.getRealName());
			s.setIdent(user.getLogin());
			s.setHost(user.getHostmask());
			s.setServer(server);
			s.setType(EventType.Nick);
			s.setChannel("");
			s.setTime(new Date(nc.getTimestamp()));
			if (s.getDetail() != null) s.getDetail().clear();
			if (channels != null) s.addDetail("channels", StringUtils.collectionToCommaDelimitedString(channels));
			s.addDetail("new.nick", nc.getNewNick());
			s = seenRepository.save(s);
		}
	}
	
	@Override
	public void handleCommand(GenericMessageEvent<PircBotX> event, Command command, User user, String[] arguments) {
		String nick = null;
		if (arguments.length == 1) {
			nick = arguments[0];
		}
		if (nick != null) {
			org.pircbotx.User user_ = event.getBot().getUserChannelDao().getUser(nick);
			Set<String> channels = null;
			if (user_ != null) {
				ImmutableSortedSet<Channel> chans = event.getBot().getUserChannelDao().getChannels(user_);
				if (chans != null && !chans.isEmpty()) {
					channels = new TreeSet<String>();
					for (Channel c : chans) {
						channels.add(c.getName());
					}
				}
			}
			Formats formats = getQuizMessages().getFormats();
			if (nick.equalsIgnoreCase(user.getNick())) {
				event.respond(String.format(formats.getSeenSelfFormat(), Colors.smartColoredNick(user.getNick())));
			} else if (nick.equalsIgnoreCase(event.getBot().getNick())) {
				// nothing
			} else if (channels != null) {
				event.respond(String.format(formats.getSeenOnlineFormat(), Colors.smartColoredNick(nick), channels));
			} else {
				String result = find(nick, event.getBot().getConfiguration().getServerHostname());
				if (result == null) {
					event.respond(String.format(formats.getSeenNotFoundFormat(), Colors.smartColoredNick(nick)));
				} else {
					event.respond(result);
				}
			}
		}
	}

	public String find(String nick, String server) {
		return wrap(nick, findSeen(nick, server));
	}

	public Seen findSeen(String nick, String server) {
		return seenRepository.findByServerAndNickIgnoreCase(server, nick);
	}

	private String wrap(String nick, Seen entity) {
		if (entity == null) {
			return null;
		}
		Formats formats = getQuizMessages().getFormats();
		if (entity.getType() == EventType.Part) {
			return String.format(formats.getSeenPartFormat(), entity.getNick(), entity.getIdent(), entity.getHost(), entity.getName(), entity.getTime(), entity.getChannel(), entity.getDetail("part.message"));
		}
		if (entity.getType() == EventType.Nick) {
			return String.format(formats.getSeenNickFormat(), entity.getNick(), entity.getIdent(), entity.getHost(), entity.getName(), entity.getTime(), entity.getDetail("new.nick"));
		}
		if (entity.getType() == EventType.Kick) {
			String kicker = entity.getDetail("kicked.from.nick") + "!" + entity.getDetail("kicked.from.ident") + "@" + entity.getDetail("kicked.from.host");
			return String.format(formats.getSeenKickFormat(), entity.getNick(), entity.getIdent(), entity.getHost(), entity.getName(), entity.getTime(), entity.getChannel(), kicker, entity.getDetail("kicked.reason"));
		}
		if (entity.getType() == EventType.Quit) {
			return String.format(formats.getSeenQuitFormat(), entity.getNick(), entity.getIdent(), entity.getHost(), entity.getName(), entity.getTime(), entity.getDetail("quit.message"));
		}

		return null;
	}

	/**
	 * Guesses the user's primary nick.
	 * 
	 * @param nick
	 *            String representing the Nick.
	 * @return User's primary nick, a guess at their primary nick, or the
	 *         supplied nick.
	 */
	public String getBestPrimaryNick(final String nick) {
		Matcher ma = jbNickPattern.matcher(nick);

		if (ma.matches())
			return ma.group(1);

		ma = irssiNickPattern.matcher(nick);

		if (ma.matches())
			return ma.group(1);

		return nick;
	}

}
