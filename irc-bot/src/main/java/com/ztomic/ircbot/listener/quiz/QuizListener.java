package com.ztomic.ircbot.listener.quiz;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ztomic.ircbot.component.ExecutorFactory;
import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.IrcConfiguration.ChannelConfig;
import com.ztomic.ircbot.configuration.MessagesConfiguration.QuizMessages;
import com.ztomic.ircbot.listener.Command;
import com.ztomic.ircbot.listener.CommandListener;
import com.ztomic.ircbot.model.Player;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.repository.PlayerRepository;
import com.ztomic.ircbot.util.Colors;
import com.ztomic.ircbot.util.Util;

@Component
public class QuizListener extends CommandListener {
	
	protected static Logger log = LoggerFactory.getLogger(QuizListener.class);
	
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private ExecutorFactory executorFactory;
	
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;
	
	private Map<String, QuizChannelHandler> quizChannelHandlers = Collections.synchronizedMap(new HashMap<>());

	public int HINT_DELAY_SEC_CFG = 15;
	public int QUESTION_DELAY_SEC_CFG = 2;
	public char HINT_CHAR_CFG = '°';
	public String COMMAND_PREFIX_CFG = "-";

	public static final char[] VOWELS = { 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'Y', 'y' };
	public static final char[] ZERRO_RATED_CHARS = { '-', ',', '.', ';', ':', '/', '_', '+', ' ', '!', '?', '\\', '\'' };

	public double FIRST_CHAR_POINTS_CFG = 0.25;
	public double LAST_CHAR_POINTS_CFG = 0.25;
	public double VOWEL_POINTS_CFG = 0.25;
	public double NUMBER_POINTS_CFG = 1;
	public double LETTER_POINTS_CFG = 0.5;
	
	public final int[] BONUS_FACTORS = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
	
	public int BONUS_RANDOM_CFG = 20;
	public int BONUS_MATCH_CFG = 5;
	
	public int BONUS_ON_LENGTH_CFG = 35;
	public int BONUS_ON_LENGTH_FACTOR_CFG = 10;
	
	public int SAME_CMD_DELAY_SEC_CFG = 15;
	
	public boolean SHOW_GUESSED_CFG = true;
	public int SHOW_GUESSED_PHASE_CFG = 1;
	/** in minutes */
	public static int DUEL_ACTIVITY_TIMEOUT_CFG = 60;
	/** in minutes */
	public int CLEANER_THREAD_INTERVAL_CFG = 1;
	public boolean SEND_GREET_CFG = true;
	public boolean AUTOSTART_QUIZ_CFG = true;
	public List<String> IGNORED_NICKS_CFG = Arrays.asList("NickServ", "ChanServ", "MemoServ");
	
	public int CHANNEL_TIMEOUT_MINUTES_CFG = 0;
	
	private Map<String, Map<Command, Long>> ignoredCommands = new HashMap<String, Map<Command,Long>>();
	
	static {
		Arrays.sort(VOWELS);
		Arrays.sort(ZERRO_RATED_CHARS);
	}
	
	private ExecutorService executor = null;
	
	protected static enum QuizCommand implements Command {
		H(User.Level.NEWBIE),
		V(User.Level.NEWBIE),
		Z(User.Level.NEWBIE),
		
		DATEVIDIM(null),
		MRTVASI(null),
		MRTAVSI(null),
		ODBIJ(null),
		NECU(null),
		SVIDVOBOJI(User.Level.MASTER),
		
		PONOVI(User.Level.REGISTERED),
		ODGOVOR(User.Level.REGISTERED),
		ODG(User.Level.REGISTERED),
		
		JUMP(User.Level.REGISTERED),
		SKIP(User.Level.REGISTERED),
		PRESKOCI(User.Level.REGISTERED),
		
		START(User.Level.REGISTERED),
		STOP(User.Level.REGISTERED),
		
		SCORE(null),
		SVEO(null),
		TOP10(User.Level.REGISTERED),
		FANATICI(User.Level.REGISTERED),
		
		SAVESETTINGS(User.Level.MASTER),
		LOADSETTINGS(User.Level.MASTER),
		
		RELOAD(User.Level.MASTER),
		IGNORENICK(User.Level.MASTER),
		UNIGNORENICK(User.Level.MASTER),
		SET(User.Level.MASTER),
		GET(User.Level.MASTER),
		SETNEXT(User.Level.MASTER),
		DUMP(User.Level.MASTER),
		LOAD(User.Level.MASTER);

		User.Level level = Level.NEWBIE;

		QuizCommand(User.Level level) {
			if (level == null) {
				level = Level.NEWBIE;
			}
			this.level = level;
		}
		
		@Override
		public String getName() {
			return name();
		}

		public User.Level getLevel() {
			return level;
		}
	}
	
	@PostConstruct
	public void created() {
		executor = executorFactory.createPersistenceThreadPoolExecutor(1000);
	}
	
	@Scheduled(cron = "0 0 0 * * ?")
	public void cleanScore() {
		log.debug("Resetting score...");
		synchronized (quizChannelHandlers) {
			for (QuizChannelHandler handler : quizChannelHandlers.values()) {
				handler.cleanScore();
			}
		}
		log.debug("Resetting score done.");
	}
	
	@Scheduled(cron = "0 0/5 * * * ?")
	public void cleanChannelHandlers() {
		log.debug("Cleaning channel handlers...");
		synchronized (quizChannelHandlers) {
			for (QuizChannelHandler handler : quizChannelHandlers.values()) {
				handler.clean();
			}
		}
		log.debug("Cleaning channel handlers done.");
	}

	public Set<? extends Command> getCommands() {
		return EnumSet.allOf(QuizCommand.class);
	}
	
	public String getName() {
		return "Quiz";
	}
	
	public String getCommandPrefix() {
		return COMMAND_PREFIX_CFG;
	}
	
	private boolean isIgnoredNick(String nick) {
		for (String n : IGNORED_NICKS_CFG) {
			if (n.equalsIgnoreCase(nick)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onJoin(JoinEvent<PircBotX> cj) {
		if (!cj.getUser().getNick().equals(cj.getBot().getUserBot().getNick())) {
			if (SEND_GREET_CFG) {
				List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(cj.getBot().getConfiguration().getServerHostname(), cj.getChannel().getName());
				Player player = null;
				for (Player p : players) {
					if (p.getNick().equalsIgnoreCase(cj.getUser().getNick())) {
						player = p;
						break;
					}
				}
				
				QuizMessages messages = getQuizMessages(cj.getBot().getConfiguration().getServerHostname(), cj.getChannel().getName());
				
				if (player != null) {
					Collections.sort(players, Player.CMP_BY_SCORE);
					int scorePos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_MONTH_SCORE);
					int monthPos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_WEEK_SCORE);
					int weekPos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_SPEED_ASC);
					int speedPos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_STREAK_ASC);
					int streekPos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_DUELS);
					int duelsPos = players.indexOf(player) + 1;
					Collections.sort(players, Player.CMP_BY_DUELS_WON);
					int duelsWonPos = players.indexOf(player) + 1;
					if (scorePos <= 10) {
						cj.getBot().sendIRC().message(cj.getChannel().getName(), Colors.paintString(Colors.YELLOW, Colors.BLACK, messages.getRandomGreetBigPlayer()) + Colors.smartColoredNick(player.getNick()));
					} else {
						cj.getBot().sendIRC().message(cj.getChannel().getName(), Colors.paintString(Colors.YELLOW, Colors.BLACK, messages.getRandomGreetNormalPlayer()) + Colors.smartColoredNick(player.getNick()));
					}
					
					cj.getBot().sendIRC().message(cj.getChannel().getName(), String.format(messages.getFormats().getJoinStatsFormat(), Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
					
				} else {
					cj.getBot().sendIRC().message(cj.getChannel().getName(), Colors.paintString(Colors.YELLOW, Colors.BLACK, messages.getRandomGreetNewbie()) + Colors.smartColoredNick(cj.getUser().getNick()));
				}
			}
		} else {
			log.info("We have joined! " + cj);
			IrcConfiguration.ChannelConfig channelConfig = ircConfiguration.getChannel(cj.getBot().getConfiguration().getServerHostname(), cj.getChannel().getName());
			if (channelConfig != null && channelConfig.isQuiz()) {
				startQuiz(cj.getBot(), cj.getChannel().getName(), null);
			}
		}
	}
	
	QuizMessages getQuizMessages(String server, String channel) {
		ChannelConfig channelConfig = ircConfiguration.getChannel(server, channel);
		if (channelConfig != null) {
			return messagesConfiguration.getQuizMessages(channelConfig.getLanguage());
		}
		return messagesConfiguration.getQuizMessages(null);
	}
	
	@Override
	public void handleMessage(GenericMessageEvent<PircBotX> msg) {
		if (!(msg instanceof MessageEvent) && !(msg instanceof PrivateMessageEvent)) {
			return;
		}
		if (isIgnoredNick(msg.getUser().getNick())) {
			return;
		}
		
		if (msg instanceof MessageEvent) {
			QuizChannelHandler handler = getQuizChannelHandler(msg.getBot(), ((MessageEvent<PircBotX>) msg).getChannel().getName(), null, false);
			if (handler != null) {
				handler.handle(msg);
			} else {
				log.debug("Not found quiz channel handler for key {}, valid keys are: {}", msg.getBot().getConfiguration().getServerHostname() + ":" + ((MessageEvent<PircBotX>) msg).getChannel().getName(), quizChannelHandlers.keySet());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void handleCommand(GenericMessageEvent<PircBotX> event, Command c, User user, String[] arguments) {
		QuizCommand command = (QuizCommand) c;
		if (command == null) {
			return;
		}
		List<String> args = Arrays.asList(arguments);
		
		String channel = null;
		if (event instanceof MessageEvent) {
			channel = ((MessageEvent) event).getChannel().getName();
		}
		
		if (user.getLevel() == User.Level.NEWBIE) {
			clearIgnoredCommands();
			synchronized (ignoredCommands) {
				Map<Command, Long> map = ignoredCommands.get(user.getNick().toLowerCase());
				if (map != null) {
					Long time = map.get(command);
					if (time != null && time >= System.currentTimeMillis()) {
						log.debug("Ignoring user's [" + user + "] command [" + command + "] with arguments [" + args + "] due to possible spam!");
						return;
					} else {
						map.put(command, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(SAME_CMD_DELAY_SEC_CFG));
					}
				} else {
					map = new HashMap<Command, Long>();
					ignoredCommands.put(user.getNick().toLowerCase(), map);
					map.put(command, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(SAME_CMD_DELAY_SEC_CFG));
				}
			}
		}
		
		switch (command) {
		case START:
			String lang = null;
			if (args != null && args.size() >= 1) {
				lang = args.get(0);
			}
			startQuiz(event.getBot(), channel, lang);
			break;
		case STOP:
			stopQuiz(event.getBot(), channel);
			log.info("User " + user + " has stopped quiz at " + channel);
			event.getBot().sendIRC().message(channel, "Kviz je zaustavljen.");
			break;
		case SET:
			if (args != null && args.size() >= 2) {
				String param = args.get(0);
				String value = args.get(1);
				try {
					boolean found = false;
					if (StringUtils.hasText(value)) {
						for (Field f : this.getClass().getFields()) {
							if (f.getName().equalsIgnoreCase(param + "_CFG")) {
								Object old = f.get(this);
								if (f.getType() == int.class || f.getType() == Integer.class) {

									int _value = Util.parseInt(value, f.getInt(this));
									f.setInt(this, _value);
								} else if (f.getType() == double.class || f.getType() == Double.class) {
									double _value = 0d;
									try {
										_value = Double.parseDouble(value);
									} catch (NumberFormatException ne) {
										_value = f.getDouble(this);
									}
									f.setDouble(this, _value);
								} else if (f.getType() == String.class) {
									f.set(this, value);
								} else if (f.getType() == char.class || f.getType() == Character.class) {
									f.setChar(this, value.charAt(0));
								} else if (f.getType() == boolean.class || f.getType() == Boolean.class) {
									f.setBoolean(this, Util.parseBool(value));
								} else if (List.class.isAssignableFrom(f.getType())) {
									f.set(this, Util.parseList(value, ","));
								}
								found = true;
								event.getBot().sendIRC().message(user.getNick(), String.format(getQuizMessages().getFormats().getChangedSettingFormat(), param, old, f.get(this)));
								break;
							}
						}
					}
					if (!found) {
						event.getBot().sendIRC().message(user.getNick(), "No config found. See GET command for available options.");
					}
				} catch (IllegalArgumentException e) {
					log.error("Error with setting field (SET cmd)..", e);
				} catch (IllegalAccessException e) {
					log.error("Error with setting field (SET cmd)..", e);
				}
				break;
			}
			break;
		case GET:
			List<String> resp = new ArrayList<String>();
			for (Field f : this.getClass().getFields()) {
				if (f.getName().endsWith("_CFG")) {
					f.setAccessible(true);
					try {
						resp.add(Colors.paintString(Colors.BLUE, f.getName().substring(0, f.getName().indexOf("_CFG"))) + " (" + Colors.paintString(Colors.DARK_GREEN, f.getType().getSimpleName()) + ") =" + Colors.paintString(Colors.RED, f.get(this)));
					} catch (Throwable t) {
					}
				}
			}
			if (!resp.isEmpty()) {
				event.getBot().sendIRC().message(user.getNick(), Util.formatList(resp, "; "));
			}
			break;
		case IGNORENICK: {
			if (args != null && args.size() == 1) {
				String nick = args.get(0).trim();
				if (isIgnoredNick(nick)) {
					event.getBot().sendIRC().message(user.getNick(), "Nick [" + nick + "] is already ignored");
				} else {
					IGNORED_NICKS_CFG.add(nick.trim());
					event.getBot().sendIRC().message(user.getNick(), "Nick [" + nick + "] added to ignore list. New list: " + Util.formatList(IGNORED_NICKS_CFG, ","));
				}
			}
			break;
		}
		case UNIGNORENICK: {
			if (args != null && args.size() == 1) {
				String nick = args.get(0).trim();
				if (!isIgnoredNick(nick)) {
					event.getBot().sendIRC().message(user.getNick(), "Nick [" + nick + "] is not ignored");
				} else {
					for (Iterator<String> iter = IGNORED_NICKS_CFG.iterator(); iter.hasNext();) {
						String n = iter.next();
						if (n.equalsIgnoreCase(nick)) {
							iter.remove();
						}
					}
					event.getBot().sendIRC().message(user.getNick(), "Nick [" + nick + "] removed from ignore list. New list: " + Util.formatList(IGNORED_NICKS_CFG, ","));
				}
			}
			break;
		}
		default:
			QuizChannelHandler quizChannelHandler = getQuizChannelHandler(event.getBot(), channel, null, false);
			if (quizChannelHandler != null) {
				quizChannelHandler.handleCommand(event, command, user, arguments);
			} else {
				log.debug("Not found quiz channel handler for key {}, valid keys are: {}", event.getBot().getConfiguration().getServerHostname() + ":" + channel, quizChannelHandlers.keySet());
			}
			break;
		}
	}
	
	private void clearIgnoredCommands() {
		synchronized (ignoredCommands) {
			for (Iterator<Map<Command, Long>> iterator = ignoredCommands.values().iterator(); iterator.hasNext();) {
				Map<Command, Long> next = iterator.next();
				for (Iterator<Long> iterator2 = next.values().iterator(); iterator2.hasNext();) {
					Long value = iterator2.next();
					if (value <= System.currentTimeMillis()) {
						iterator2.remove();
					}
				}
				if (next.isEmpty()) {
					iterator.remove();
				}
			}
		}
	}
	
	public void stopQuiz(PircBotX bot, String channel) {
		QuizChannelHandler handler = removeQuizChannelHandler(bot, channel);
		if (handler != null) {
			handler.stopQuiz();
		}
	}
	
	public QuizChannelHandler removeQuizChannelHandler(PircBotX bot, String channel) {
		String key = bot.getConfiguration().getServerHostname() + ":" + channel;
		return quizChannelHandlers.remove(key);
	}
	
	public QuizChannelHandler getQuizChannelHandler(PircBotX bot, String channel, String language, boolean create) {
		String key = bot.getConfiguration().getServerHostname() + ":" + channel;
		QuizChannelHandler quizChannelHandler = quizChannelHandlers.get(key);
		if (!StringUtils.hasText(language)) {
			IrcConfiguration.ChannelConfig chan = ircConfiguration.getChannel(bot.getConfiguration().getServerHostname(), channel);
			if (chan != null) {
				language = chan.getLanguage();
			}
		}
		if (quizChannelHandler == null && create) {
			quizChannelHandler = new QuizChannelHandler(bot, this, channel, language);
			beanFactory.autowireBean(quizChannelHandler);
			quizChannelHandlers.put(key, quizChannelHandler);
		}
		return quizChannelHandler;
	}
	
	public void startQuiz(PircBotX bot, String channel, String language) {
		QuizChannelHandler quizChannelHandler = getQuizChannelHandler(bot, channel, language, true);
		if (!quizChannelHandler.alive) {
			executor.execute(quizChannelHandler);
		}
	}

	
}
