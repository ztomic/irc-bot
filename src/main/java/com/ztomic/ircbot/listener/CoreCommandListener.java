package com.ztomic.ircbot.listener;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.repository.UserRepository;
import org.pircbotx.Channel;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSortedSet;
import com.ztomic.ircbot.IrcBotApplication;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.util.Colors;

@Component
public class CoreCommandListener extends CommandListener {

	public enum CoreCommand implements Command {
		NICK(Level.MASTER), 
		JOIN(Level.MASTER), 
		PART(Level.MASTER), 
		MSG(Level.MASTER),
		NOTICE(Level.MASTER),
		ACTION(Level.MASTER),
		MODE(Level.MASTER),
		SHUTDOWN(Level.MASTER), 
		RESTART(Level.MASTER),
		REGISTERMASTER(Level.NEWBIE),
		USERINFO(Level.MASTER),
		RAW(Level.MASTER),
		SETLEVEL(Level.MASTER),
		LUSERS(Level.MASTER),
		TEST(Level.MASTER);

		public final Level level;

		CoreCommand(Level level) {
			this.level = level;
		}
		
		@Override
		public String getName() {
			return name();
		}
		
		@Override
		public Level getLevel() {
			return level;
		}
		
	}

	public CoreCommandListener(IrcConfiguration ircConfiguration, MessagesConfiguration messagesConfiguration, UserRepository userRepository) {
		super(ircConfiguration, messagesConfiguration, userRepository);
	}
	
	@Override
	public String getName() {
		return "Core";
	}

	@Override
	public Set<? extends Command> getCommands() {
		return EnumSet.allOf(CoreCommand.class);
	}

	@Override
	public void handleCommand(GenericMessageEvent event, Command command, User user, String[] arguments) {
		if (!(command instanceof CoreCommand coreCommand)) {
			return;
		}
		List<String> args = Arrays.asList(arguments);
		switch (coreCommand) {
			case NICK -> {
				if (args.size() == 1) {
					event.getBot().sendIRC().changeNick(args.getFirst());
				}
			}
			case JOIN -> {
				if (args.size() >= 1) {
					event.getBot().sendIRC().joinChannel(args.getFirst());
				}
			}
			case PART -> {
				if (args.size() == 1) {
					event.getBot().sendRaw().rawLine("PART " + args.getFirst());
				} else if (args.size() > 1) {
					event.getBot().sendRaw().rawLine("PART " + args.getFirst() + " :" + formatCollection(args.subList(1, args.size()), " "));
				}
			}
			case MSG -> {
				if (args.size() >= 2) {
					event.getBot().sendIRC().message(args.getFirst(), formatCollection(args.subList(1, args.size()), " "));
				}
			}
			case NOTICE -> {
				if (args.size() >= 2) {
					event.getBot().sendIRC().notice(args.getFirst(), formatCollection(args.subList(1, args.size()), " "));
				}
			}
			case ACTION -> {
				if (args.size() >= 2) {
					event.getBot().sendIRC().action(args.getFirst(), formatCollection(args.subList(1, args.size()), " "));
				}
			}
			case MODE -> {
				if (args.size() >= 2) {
					event.getBot().sendIRC().mode(args.getFirst(), formatCollection(args.subList(1, args.size()), " "));
				}
			}
			case RAW -> event.getBot().sendRaw().rawLine(formatCollection(args, " "));
			case LUSERS -> {
				String channel = null;
				if (args.size() == 1) {
					channel = args.getFirst();
				}
				Channel chan = null;
				if (StringUtils.hasText(channel)) {
					chan = event.getBot().getUserChannelDao().getChannel(channel);
				}
				if (chan != null) {
					ImmutableSortedSet<org.pircbotx.User> users = chan.getUsers();
					if (users != null) {
						Set<String> nicks = users.stream().map(org.pircbotx.User::getNick).collect(Collectors.toCollection(TreeSet::new));
						event.getUser().send().message("{C}3Users on channel [" + channel + "]:\n" + formatCollection(nicks, "\n"));
					}
				} else {
					for (Channel channel_ : event.getBot().getUserChannelDao().getAllChannels()) {
						ImmutableSortedSet<org.pircbotx.User> users = channel_.getUsers();
						if (users != null) {
							Set<String> nicks = users.stream().map(org.pircbotx.User::getNick).collect(Collectors.toCollection(TreeSet::new));
							event.getUser().send().message("{C}3Users on channel [" + channel_.getName() + "]:\n" + formatCollection(nicks, "\n"));
						}
					}
				}
			}
			case SHUTDOWN -> {
				for (Channel channel_ : event.getBot().getUserChannelDao().getAllChannels()) {
					channel_.send().message("{C}4Shutting down...");
				}
				IrcBotApplication.close(false);
			}
			case RESTART -> {
				for (Channel channel_ : event.getBot().getUserChannelDao().getAllChannels()) {
					channel_.send().message("{C}4Restarting...");
				}
				IrcBotApplication.close(true);
			}
			case TEST -> {
				if (args.size() >= 1) {
					event.getUser().send().message(Colors.paintString(formatCollection(args, " ")));
				} else {
					String TEST_FORMAT = getQuizMessages().getFormats().getTestFormat();
					event.getUser().send().message(String.format(Colors.paintString(TEST_FORMAT), user.getNick()));
					event.getUser().send().message(Colors.paintString(String.format(TEST_FORMAT, user.getNick())));
				}
			}
			case USERINFO -> {
				String _nick = user.getNick();
				if (args.size() >= 1) {
					_nick = args.getFirst();
				}
				User _user = userRepository.findByServerAndNickIgnoreCase(event.getBot().getServerHostname(), _nick);
				if (_user != null) {
					event.getUser().send().message("{C}3UserInfo: " + _user);
				} else {
					event.getUser().send().message("{C}4User with nick " + _nick + " not found.");
				}
			}
			case REGISTERMASTER -> {
				// ?REGISTERMASTER nick secretKey
				if (args.size() == 2) {
					String _nick = args.get(0);
					String _key = args.get(1);

					if (StringUtils.hasText(_nick) && StringUtils.hasText(_key)) {
						if (_nick.equals(user.getNick()) && _key.equals(ircConfiguration.getSecretMasterKey())) {
							try {
								user.setLevel(Level.MASTER);
								userRepository.saveAndFlush(user);
							} catch (Throwable t) {
								log.error("Error changing level to MASTER for user: {}", user, t);
							}
							event.getUser().send().message("{C}3Changed level to MASTER.!");
						} else if (_nick.equals(user.getNick())) {
							event.getUser().send().message("{C}4Invalid password!");
						} else {
							event.getUser().send().message("{C}4Invalid usage!");
						}
					}
				}
			}
			case SETLEVEL -> {
				if (args.size() == 2) {
					String _nick = args.get(0);
					String _level = args.get(1);
					if (StringUtils.hasText(_nick) && StringUtils.hasText(_level)) {
						User _user = userRepository.findByServerAndNickIgnoreCase(event.getBot().getServerHostname(), _nick);
						if (_user != null) {
							Level _old = _user.getLevel();
							Level level = Level.valueOf(_level, _user.getLevel());
							try {
								_user.setLevel(level);
								_user = userRepository.saveAndFlush(_user);
							} catch (Throwable t) {
								log.error("Error changing level of user: {}", _user, t);
							}
							event.getUser().send().message("{C}3Level for nick " + _nick + " changed from " + _old + " to " + level);
						} else {
							event.getUser().send().message("{C}4Nick " + _nick + " not found.");
						}

					}
				}
			}
		}
	}

}
