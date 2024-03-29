package com.ztomic.ircbot.listener.quiz;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import com.ztomic.ircbot.component.ExecutorFactory;
import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.configuration.IrcConfiguration.ChannelConfig;
import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration.QuizMessages;
import com.ztomic.ircbot.listener.Command;
import com.ztomic.ircbot.listener.CommandListener;
import com.ztomic.ircbot.model.Player;
import com.ztomic.ircbot.model.Question;
import com.ztomic.ircbot.model.QuestionError;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.model.User.Level;
import com.ztomic.ircbot.repository.PlayerRepository;
import com.ztomic.ircbot.repository.QuestionErrorRepository;
import com.ztomic.ircbot.repository.QuestionRepository;
import com.ztomic.ircbot.repository.UserRepository;
import com.ztomic.ircbot.util.Colors;
import com.ztomic.ircbot.util.Util;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

@Component
public class QuizListener extends CommandListener {
	
	protected static Logger log = LoggerFactory.getLogger(QuizListener.class);

	private final QuestionRepository questionRepository;
	private final QuestionErrorRepository questionErrorRepository;
	private final PlayerRepository playerRepository;
	private final ExecutorFactory executorFactory;
	private final ConfigurableListableBeanFactory beanFactory;
	private final QuizSettings quizSettings;
	
	private final Map<String, QuizChannelHandler> quizChannelHandlers = Collections.synchronizedMap(new HashMap<>());

	public static final char[] VOWELS = { 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'Y', 'y' };
	public static final char[] ZERRO_RATED_CHARS = { '-', ',', '.', ';', ':', '/', '_', '+', ' ', '!', '?', '\\', '\'' };
	public final int[] BONUS_FACTORS = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
	
	private final Map<String, Map<Command, Long>> ignoredCommands = new HashMap<>();
	
	static {
		Arrays.sort(VOWELS);
		Arrays.sort(ZERRO_RATED_CHARS);
	}
	
	private ExecutorService executor = null;

	public QuizListener(IrcConfiguration ircConfiguration, MessagesConfiguration messagesConfiguration, UserRepository userRepository, QuestionRepository questionRepository, QuestionErrorRepository questionErrorRepository, PlayerRepository playerRepository, ExecutorFactory executorFactory, ConfigurableListableBeanFactory beanFactory) {
		super(ircConfiguration, messagesConfiguration, userRepository);
		this.questionRepository = questionRepository;
		this.questionErrorRepository = questionErrorRepository;
		this.playerRepository = playerRepository;
		this.executorFactory = executorFactory;
		this.beanFactory = beanFactory;
		this.quizSettings = new QuizSettings();
	}

	protected enum QuizCommand implements Command {
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
		TOP3(User.Level.REGISTERED),
		
		SAVESETTINGS(User.Level.MASTER),
		LOADSETTINGS(User.Level.MASTER),
		
		RELOAD(User.Level.MASTER),
		IGNORENICK(User.Level.MASTER),
		UNIGNORENICK(User.Level.MASTER),
		SET(User.Level.MASTER),
		GET(User.Level.MASTER),
		SETNEXT(User.Level.MASTER),
		DUMP(User.Level.MASTER),
		LOAD(User.Level.MASTER),

		ERROR(User.Level.MASTER);

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
		executor = executorFactory.createPersistenceThreadPoolExecutor("quiz-handler", 1000);
	}
	
	@Scheduled(cron = "0 0 0 * * ?")
	public void cleanScore() {
		log.debug("Resetting score...");
		synchronized (quizChannelHandlers) {
			quizChannelHandlers.values().forEach(com.ztomic.ircbot.listener.quiz.QuizChannelHandler::cleanScore);
		}
		log.debug("Resetting score done.");
	}
	
	@Scheduled(cron = "0 0/5 * * * ?")
	public void cleanChannelHandlers() {
		log.debug("Cleaning channel handlers...");
		synchronized (quizChannelHandlers) {
			quizChannelHandlers.values().forEach(com.ztomic.ircbot.listener.quiz.QuizChannelHandler::clean);
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
		return quizSettings.getCommandPrefix();
	}
	
	private boolean isIgnoredNick(String nick) {
		for (String n : quizSettings.getIgnoredNicks()) {
			if (n.equalsIgnoreCase(nick)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onJoin(JoinEvent cj) {
		if (!cj.getUser().getNick().equals(cj.getBot().getUserBot().getNick())) {
			if (quizSettings.isSendGreet()) {
				List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(cj.getBot().getServerHostname(), cj.getChannel().getName());
				Player player = players
						.stream()
						.filter(p -> p.getNick().equalsIgnoreCase(cj.getUser().getNick()))
						.findFirst().orElse(null);
				
				QuizMessages messages = getQuizMessages(cj.getBot().getServerHostname(), cj.getChannel().getName());
				
				if (player != null) {
					players.sort(Player.CMP_BY_SCORE);
					int scorePos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_MONTH_SCORE);
					int monthPos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_WEEK_SCORE);
					int weekPos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_SPEED_ASC);
					int speedPos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_STREAK_ASC);
					int streekPos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_DUELS);
					int duelsPos = players.indexOf(player) + 1;
					players.sort(Player.CMP_BY_DUELS_WON);
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
			log.info("I have joined! {}", cj);
			IrcConfiguration.ChannelConfig channelConfig = ircConfiguration.getChannel(cj.getBot().getServerHostname(), cj.getChannel().getName());
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
	public void handleMessage(GenericMessageEvent msg) {
		if (!(msg instanceof MessageEvent) && !(msg instanceof PrivateMessageEvent)) {
			return;
		}
		if (isIgnoredNick(msg.getUser().getNick())) {
			return;
		}
		
		if (msg instanceof MessageEvent) {
			QuizChannelHandler handler = getQuizChannelHandler(msg.getBot(), ((MessageEvent) msg).getChannel().getName(), null, false);
			if (handler != null) {
				handler.handle(msg);
			} else {
				log.debug("Not found quiz channel handler for key {}, valid keys are: {}", msg.getBot().getServerHostname() + ":" + ((MessageEvent) msg).getChannel().getName(), quizChannelHandlers.keySet());
			}
		}
	}

	@Override
	public void handleCommand(GenericMessageEvent event, Command c, User user, String[] arguments) {
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
						log.debug("Ignoring user's [{}] command [{}] with arguments [{}] due to possible spam!", user, command, args);
						return;
					} else {
						map.put(command, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(quizSettings.getSameCmdDelaySec()));
					}
				} else {
					map = new HashMap<>();
					ignoredCommands.put(user.getNick().toLowerCase(), map);
					map.put(command, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(quizSettings.getSameCmdDelaySec()));
				}
			}
		}

		switch (command) {
			case START -> {
				String lang = null;
				if (args.size() >= 1) {
					lang = args.getFirst();
				}
				startQuiz(event.getBot(), channel, lang);
			}
			case STOP -> {
				stopQuiz(event.getBot(), channel);
				log.info("User {} has stopped quiz at {}", user, channel);
				event.getBot().sendIRC().message(channel, "{C}4Kviz je zaustavljen.");
			}
			case SET -> {
				if (args.size() >= 2) {
					String param = args.get(0);
					String value = args.get(1);
					try {
						if (StringUtils.hasText(value)) {
							Field f = ReflectionUtils.findField(quizSettings.getClass(), param);
							if (f != null) {
								ReflectionUtils.makeAccessible(f);
								Object old = ReflectionUtils.getField(f, quizSettings);
								if (f.getType() == int.class || f.getType() == Integer.class) {
									int _value = Util.parseInt(value, f.getInt(quizSettings));
									f.setInt(quizSettings, _value);
								} else if (f.getType() == double.class || f.getType() == Double.class) {
									double _value = 0d;
									try {
										_value = Double.parseDouble(value);
									} catch (NumberFormatException ne) {
										_value = f.getDouble(quizSettings);
									}
									f.setDouble(quizSettings, _value);
								} else if (f.getType() == String.class) {
									f.set(quizSettings, value);
								} else if (f.getType() == char.class || f.getType() == Character.class) {
									f.setChar(quizSettings, value.charAt(0));
								} else if (f.getType() == boolean.class || f.getType() == Boolean.class) {
									f.setBoolean(quizSettings, Util.parseBool(value));
								} else if (List.class.isAssignableFrom(f.getType())) {
									f.set(quizSettings, Util.parseList(value, ","));
								}
								event.getBot().sendIRC().message(user.getNick(), String.format(getQuizMessages().getFormats().getChangedSettingFormat(), param, old, f.get(quizSettings)));
							} else {
								event.getBot().sendIRC().message(user.getNick(), "{C}4No config{C}3 " + param + " {C}4found. See GET command for available options.");
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("Error with setting field (SET cmd)..", e);
					}
				}
			}
			case GET -> {
				List<String> resp = new ArrayList<>();
				ReflectionUtils.doWithFields(quizSettings.getClass(), f -> {
					try {
						ReflectionUtils.makeAccessible(f);
						resp.add(Colors.paintString(Colors.BLUE, f.getName()) + " (" + Colors.paintString(Colors.DARK_GREEN, f.getType().getSimpleName()) + ") =" + Colors.paintString(Colors.RED, f.get(quizSettings)));
					} catch (Throwable t) {
						//
					}
				});
				if (!resp.isEmpty()) {
					event.getBot().sendIRC().message(user.getNick(), formatCollection(resp, "; "));
				}
			}
			case IGNORENICK -> {
				if (args.size() == 1) {
					String nick = args.getFirst().trim();
					if (isIgnoredNick(nick)) {
						event.getBot().sendIRC().message(user.getNick(), "{C}4Nick [" + nick + "] is already ignored");
					} else {
						quizSettings.getIgnoredNicks().add(nick.trim());
						event.getBot().sendIRC().message(user.getNick(), "{C}3Nick [" + nick + "] added to ignore list. New list: " + formatCollection(quizSettings.getIgnoredNicks(), ","));
					}
				}
			}
			case UNIGNORENICK -> {
				if (args.size() == 1) {
					String nick = args.getFirst().trim();
					if (!isIgnoredNick(nick)) {
						event.getBot().sendIRC().message(user.getNick(), "{C}4Nick [" + nick + "] is not ignored");
					} else {
						quizSettings.getIgnoredNicks().removeIf(n -> n.equalsIgnoreCase(nick));
						event.getBot().sendIRC().message(user.getNick(), "{C}3Nick [" + nick + "] removed from ignore list. New list: " + formatCollection(quizSettings.getIgnoredNicks(), ","));
					}
				}
			}
			case ERROR -> {
				if (args.size() >= 2) {
					log.info("User {} is reporting error at question {}", user, args);
					long questionNumber = Util.parseLong(args.getFirst(), -1);
					if (questionNumber != -1) {
						Optional<Question> question = questionRepository.findById(questionNumber);
						if (question.isPresent()) {
							Question q = question.get();
							String reason = String.join(" ", args.subList(1, args.size()));
							QuestionError error = new QuestionError();
							error.setQuestionId(q.getId());
							error.setReason(reason);
							error.setUserId(user.getId());
							error.setTimeReported(LocalDateTime.now());
							error = questionErrorRepository.save(error);
							event.getBot().sendIRC().message(user.getNick(), "Vasa prijava broj " + Colors.paintString(Colors.BLUE, error.getId()) + " za pitanje " + String.format(getQuizMessages().getFormats().getQuestionFormat(), q.getId(), q.getTheme(), q.getQuestion()) + " sa razlogom " + Colors.paintString(Colors.RED, reason) + " je zabiljezena. Hvala!");
						} else {
							event.getBot().sendIRC().message(user.getNick(), "Pitanje sa brojem " + Colors.paintString(Colors.DARK_GREEN, questionNumber) + " nije pronađeno.");
						}
					} else {
						event.getBot().sendIRC().message(user.getNick(), "Vasa prijava nije ispravna! Molimo prijavite u formatu: " + getCommandPrefix() + QuizCommand.ERROR.name() + " " + Colors.paintString(Colors.DARK_GREEN, "BROJPITANJA") + " " + Colors.paintString(Colors.RED, "razlog"));
					}
				} else {
					event.getBot().sendIRC().message(user.getNick(), "Vasa prijava nije ispravna! Molimo prijavite u formatu: " + getCommandPrefix() + QuizCommand.ERROR.name() + " " + Colors.paintString(Colors.DARK_GREEN, "BROJPITANJA") + " " + Colors.paintString(Colors.RED, "razlog"));
				}
			}
			default -> {
				QuizChannelHandler quizChannelHandler = getQuizChannelHandler(event.getBot(), channel, null, false);
				if (quizChannelHandler != null) {
					quizChannelHandler.handleCommand(event, command, user, arguments);
				} else {
					log.debug("Not found quiz channel handler for key {}, valid keys are: {}", event.getBot().getServerHostname() + ":" + channel, quizChannelHandlers.keySet());
				}
			}
		}
	}
	
	private void clearIgnoredCommands() {
		synchronized (ignoredCommands) {
			for (Iterator<Map<Command, Long>> iterator = ignoredCommands.values().iterator(); iterator.hasNext();) {
				Map<Command, Long> next = iterator.next();
				next.values().removeIf(value -> value <= System.currentTimeMillis());
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
		String key = bot.getServerHostname() + ":" + channel;
		return quizChannelHandlers.remove(key);
	}
	
	public QuizChannelHandler getQuizChannelHandler(PircBotX bot, String channel, String language, boolean create) {
		String key = bot.getServerHostname() + ":" + channel;
		QuizChannelHandler quizChannelHandler = quizChannelHandlers.get(key);
		if (!StringUtils.hasText(language)) {
			IrcConfiguration.ChannelConfig chan = ircConfiguration.getChannel(bot.getServerHostname(), channel);
			if (chan != null) {
				language = chan.getLanguage();
			}
		}
		if (quizChannelHandler == null && create) {
			quizChannelHandler = new QuizChannelHandler(quizSettings, questionRepository, playerRepository, userRepository, messagesConfiguration, bot, this, channel, language);
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
