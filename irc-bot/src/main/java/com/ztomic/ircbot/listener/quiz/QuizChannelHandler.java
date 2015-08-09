package com.ztomic.ircbot.listener.quiz;

import static com.ztomic.ircbot.configuration.Formats.CHANNEL_STREAK_RECORD_COMMENT_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.FIRST_CHAR_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.GUESSED_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.HINT1_BONUS_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.HINT1_CHALLENGE_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.HINT1_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.HINT2_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.HINT3_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.JOIN_STATS_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.LAST_ANSWER_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.LAST_CHAR_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.QUESTION_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.TIMEUP_MESSAGE_FORMAT;
import static com.ztomic.ircbot.configuration.Formats.VOWELS_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.ztomic.ircbot.configuration.MessagesConfiguration;
import com.ztomic.ircbot.configuration.MessagesConfiguration.QuizMessages;
import com.ztomic.ircbot.listener.quiz.QuizListener.QuizCommand;
import com.ztomic.ircbot.model.Player;
import com.ztomic.ircbot.model.Question;
import com.ztomic.ircbot.model.User;
import com.ztomic.ircbot.repository.PlayerRepository;
import com.ztomic.ircbot.repository.QuestionRepository;
import com.ztomic.ircbot.repository.UserRepository;
import com.ztomic.ircbot.util.Colors;
import com.ztomic.ircbot.util.Util;


public class QuizChannelHandler implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(QuizChannelHandler.class);

	private static String ZERO_RATED_REGEX = "";

	static {
		for (char c : QuizListener.ZERRO_RATED_CHARS) {
			ZERO_RATED_REGEX += c;
		}
		ZERO_RATED_REGEX = "[" + Pattern.quote(ZERO_RATED_REGEX.trim()) + "]";
	}

	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MessagesConfiguration messagesConfiguration;

	private PircBotX bot;
	private QuizListener quizListener;
	private String channel;
	private String language;
	
	public Question lastQuestion;
	private long lastActivity = System.currentTimeMillis();
	public String lastAnswer;
	private Hint lastHint;
	public long lastPlayer;
	public int streak = 1;
	public boolean answered = false;
	public long points;
	public long tstart;
	public boolean alive;
	
	private List<Question> questions;
	public int nextId = 0;
	private static Random random = new Random();
	private boolean jump;
	
	private long channelFastestTime = 0;
	private long fastestPlayer;
	private long maxStreakPlayer;

	private List<Duel> duels = new ArrayList<Duel>();
	
	private Thread thread;

	public QuizChannelHandler(PircBotX bot, QuizListener quizHandler, String channel, String language) {
		this.bot = bot;
		this.quizListener = quizHandler;
		this.channel = channel;
		if (!StringUtils.hasText(language)) {
			language = "CROATIAN";
		}
		this.language = language;
	}

	public void sendMessage(String target, String message) {
		bot.sendIRC().message(target, message);
	}

	public org.pircbotx.User getUser(Channel channel, String nick) {
		for (org.pircbotx.User user : channel.getUsers()) {
			if (user.getNick().equals(nick)) {
				return user;
			}
		}
		return null;
	}

	public Channel getChannel(String channel) {
		return bot.getUserChannelDao().getChannel(channel);
	}

	public Set<org.pircbotx.User> getUsers(String channel) {
		return bot.getUserChannelDao().getUsers(getChannel(channel));
	}
	
	QuizMessages getQuizMessages() {
		return messagesConfiguration.getQuizMessages(language);
	}

	public void jump() {
		synchronized (this) {
			jump = true;
			notify();
		}
	}

	public void sendH() {
		String h = lastHint.getFirstChar();
		if (h != null) {
			sendMessage(channel, String.format(FIRST_CHAR_FORMAT, h));
		}
	}

	public void sendV() {
		String v = lastHint.getVowels();
		if (v != null) {
			sendMessage(channel, String.format(VOWELS_FORMAT, v));
		}
	}

	public void sendZ() {
		String z = lastHint.getLastChar();
		if (z != null) {
			sendMessage(channel, String.format(LAST_CHAR_FORMAT, z));
		}
	}

	public synchronized void repeatLastQuestion() {
		if (lastQuestion != null && !answered) {
			sendMessage(channel, String.format(QUESTION_FORMAT, lastQuestion.getId(), lastQuestion.getTheme(), lastQuestion.getQuestion()));
		}
	}

	public class Hint {

		private final String answer;
		private final double answerPoints;

		private boolean sentH;
		private boolean sentV;
		private boolean sentZ;

		private int phase = 1;

		private StringBuilder hint;

		public Hint(String answer) {
			this.answer = answer;
			hint = new StringBuilder();
			for (char c : answer.toCharArray()) {
				if (Arrays.binarySearch(QuizListener.ZERRO_RATED_CHARS, c) >= 0) {
					hint.append(c);
				} else {
					hint.append(quizListener.HINT_CHAR_CFG);
				}
			}
			answerPoints = calculateAnswerPoints();
		}

		private double calculateAnswerPoints() {
			double pt = 0;
			if (answer.length() == 1) {
				return random.nextInt(3);
			}
			for (int i = 0; i < answer.length(); i++) {
				char c = answer.charAt(i);
				if (c == ' ')
					continue;
				if (i == 0) {
					pt += quizListener.FIRST_CHAR_POINTS_CFG;
					continue;
				}
				if (i == answer.length() - 1) {
					pt += quizListener.LAST_CHAR_POINTS_CFG;
					continue;
				}
				if (Character.isDigit(c)) {
					pt += quizListener.NUMBER_POINTS_CFG;
					continue;
				}
				if (Arrays.binarySearch(QuizListener.VOWELS, c) >= 0) {
					pt += quizListener.VOWEL_POINTS_CFG;
					continue;
				}
				if (Arrays.binarySearch(QuizListener.ZERRO_RATED_CHARS, c) >= 0) {
					continue;
				}
				pt += quizListener.LETTER_POINTS_CFG;
			}
			if (answer.length() > quizListener.BONUS_ON_LENGTH_CFG) {
				pt += (answer.length() * quizListener.BONUS_ON_LENGTH_FACTOR_CFG);
			}
			return pt;
		}

		public long getAnswerPoints() {
			return Math.round(answerPoints);
		}

		public synchronized String getVowels() {
			if (!sentV) {
				StringBuilder vowels = new StringBuilder();
				for (int i = 0; i < answer.length(); i++) {
					char chr = answer.charAt(i);
					if (Arrays.binarySearch(QuizListener.VOWELS, chr) >= 0 || chr == ' ') {
						vowels.append(chr);
						if (hint.charAt(i) == quizListener.HINT_CHAR_CFG) {
							hint.replace(i, i + 1, String.valueOf(chr));
						}
					} else {
						vowels.append(quizListener.HINT_CHAR_CFG);
					}
				}
				sentV = true;
				return vowels.toString();
			}
			return null;
		}

		public synchronized String getFirstChar() {
			if (!sentH) {
				if (hint.charAt(0) == quizListener.HINT_CHAR_CFG) {
					hint.replace(0, 1, String.valueOf(answer.charAt(0)));
				}
				sentH = true;
				return String.valueOf(hint.charAt(0));
			}
			return null;
		}

		public synchronized String getLastChar() {
			if (!sentZ) {
				if (hint.charAt(hint.length() - 1) == quizListener.HINT_CHAR_CFG) {
					hint.replace(hint.length() - 1, hint.length(), String.valueOf(answer.charAt(answer.length() - 1)));
				}
				sentZ = true;
				return String.valueOf(hint.charAt(hint.length() - 1));
			}
			return null;
		}

		public synchronized String getLevel3Hint() {
			phase = 3;
			for (int i = 0; i < answer.length(); i++) {
				if (i % 2 == 0 || answer.charAt(i) == ' ') {
					if (hint.charAt(i) == quizListener.HINT_CHAR_CFG) {
						hint.replace(i, i + 1, String.valueOf(answer.charAt(i)));
					}
				}
			}
			return hint.toString();
		}

		public synchronized String getLevel2Hint() {
			phase = 2;
			getFirstChar();
			return hint.toString();
		}

		public synchronized String getLevel1Hint() {
			phase = 1;
			return hint.toString();
		}

		public synchronized void merge(String text) {
			String oldHint = hint.toString();
			text = text.toLowerCase();
			for (int i = 0; i < text.length(); i++) {
				if (i >= answer.length())
					break;
				char c = text.charAt(i);
				if (answer.toLowerCase().charAt(i) == c) {
					if (hint.charAt(i) == quizListener.HINT_CHAR_CFG) {
						hint.replace(i, i + 1, String.valueOf(answer.charAt(i)));
					}
				}

			}
			if (quizListener.SHOW_GUESSED_CFG && !oldHint.equals(hint.toString()) && phase >= quizListener.SHOW_GUESSED_PHASE_CFG) {
				sendMessage(channel, String.format(GUESSED_FORMAT, hint.toString()));
			}
		}
	}

	public static String getPointsString(long i) {
		if (i < 0)
			i = -i;
		i %= 100;
		if (i >= 10 && i <= 20)
			return "bodova";
		i %= 10;
		return ((i >= 5 || i == 0) ? "bodova" : (i >= 2 ? "boda" : "bod"));
	}

	public synchronized void reloadQuestions() {
		this.questions = questionRepository.findByLanguage(language);
		log.info("Reloaded questions. Question count: " + questions.size());
	}

	public boolean patternMatches(String answer, String message) {
		try {
			return answer.trim().matches(message);
		} catch (PatternSyntaxException e) {
			return false;
		}
	}

	private boolean isAnswer(String answer, String message) {
		boolean match = false;
		match = answer.trim().equalsIgnoreCase(message);
		if (!match) {
			match = answer.replaceAll(ZERO_RATED_REGEX, "").trim().equalsIgnoreCase(message);
		}

		return match;
	}

	public synchronized boolean handle(GenericMessageEvent<PircBotX> msg) {
		lastActivity = System.currentTimeMillis();
		String message = msg.getMessage();
		long time = (System.currentTimeMillis() - tstart);
		if (lastQuestion != null && !answered) {
			Player player = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(msg.getBot().getConfiguration().getServerHostname(), channel, msg.getUser().getNick());

			if (player == null) {
				player = new Player();
				player.setChannel(channel);
				player.setNick(msg.getUser().getNick());
				player.setScore(0);
				player.setServer(msg.getBot().getConfiguration().getServerHostname());
				player = playerRepository.saveAndFlush(player);
			}

			try {
				for (String answer : lastQuestion.getAnswers()) {
					User user = userRepository.findByServerAndNick(player.getServer(), player.getNick());
					if (isAnswer(answer, message) || (user != null && user.getLevel() == User.Level.MASTER && patternMatches(answer, message))) {
						answered = true;
						boolean record = false;
						if (player.getFastestTime() == 0 || player.getFastestTime() > time) {
							player.setFastestTime(time);
							record = true;
						}
						player.incrementScore(points);
						boolean hasStreak = false;
						if (lastPlayer == player.getId()) {
							streak++;
							hasStreak = true;
						} else {
							streak = 1;
						}
						String comment = getQuizMessages().getRandomAnswerComment();
						sendMessage(
								channel,
								Colors.paintString(Colors.BLUE, comment + ",") + Colors.smartColoredNick(player.getNick()) + Colors.paintString(Colors.BLUE, "! Odgovor je ->") + Colors.paintString(Colors.BOLD, Colors.paintString(Colors.DARK_BLUE, Colors.YELLOW, answer)) + Colors.paintString(Colors.BLUE, "<-. Vrijeme:") + Colors.paintString(Colors.DARK_GREEN, (time / 1000F)) + Colors.paintString(Colors.BLUE, "sec") + (record ? " (" + Colors.paintString(Colors.WHITE, Colors.RED, "OSOBNI REKORD!") + ")." : ".") + (hasStreak ? Colors.paintString(Colors.BLUE, "Niz:") + Colors.paintString(Colors.DARK_GREEN, streak) + Colors.paintString(Colors.BLUE, ".") : "") + Colors.paintString(Colors.BLUE, "Dobivate") + Colors.paintString(Colors.DARK_GREEN, points)
								+ Colors.paintString(Colors.BLUE, getPointsString(points) + ".") + Colors.paintString(Colors.BLUE, "Novi score:") + Colors.paintString(Colors.DARK_GREEN, player.getScore()));
						if (channelFastestTime == 0 || channelFastestTime > time) {
							Player fastestPlayer = getFastestPlayer() > 0 ? playerRepository.getOne(getFastestPlayer()) : null;
							sendMessage(channel, Colors.smartColoredNick(player.getNick()) + " je obori[o|la] brzinski rekord kanala" + (fastestPlayer != null ? " koji je do sada drza[o|la] " + Colors.smartColoredNick(fastestPlayer.getNick()) + " sa " + Colors.paintString(Colors.DARK_GREEN, fastestPlayer.getFastestTime() / 1000F) + "sec" : "."));
							channelFastestTime = time;
							setFastestPlayer(player.getId());
						}
						if (streak > player.getRowRecord() && hasStreak) {
							player.setRowRecord(streak);
							//sendMessage(player.nick, String.format(PERSONAL_STREAK_RECORD_COMMENT_FORMAT, player.rowRecord));
						}
						if ((getMaxStreakPlayer() <= 0 || streak > playerRepository.getOne(getMaxStreakPlayer()).getRowRecord()) && hasStreak) {
							setMaxStreakPlayer(player.getId());
							sendMessage(channel, String.format(CHANNEL_STREAK_RECORD_COMMENT_FORMAT, player.getNick(), player.getRowRecord()));
						}
						lastPlayer = player.getId();
						notify();
						if (record) {
							//sendMessage(player.nick, "Cestitamo, oborili ste osobni brzinski rekord i on sada iznosi " + Colors.paintString(Colors.RED, time / 1000F) + " sekundi");
						}

						List<Duel> foundDuels = findDuels(player.getNick());
						if (!foundDuels.isEmpty()) {
							for (Duel d : foundDuels) {
								if (!d.confirmed) continue;
								if (d.startedAt >= tstart) continue;
								d.getDuelist(player.getNick()).score++;
								d.setLastActivity(System.currentTimeMillis());
								if (d.isFinished()) {
									player.incrementDuelsWon();
									synchronized (duels) {
										duels.remove(d);
									}
									sendMessage(channel, String.format("Dvoboj izmedju %s i %s je zavrsen (%s:%s), Cestitamo %s!", Colors.smartColoredNick(d.nick1.nick), Colors.smartColoredNick(d.nick2.nick), Colors.paintString(Colors.DARK_GREEN, d.nick1.score), Colors.paintString(Colors.DARK_GREEN, d.nick2.score), Colors.smartColoredNick(player.getNick())));
								} else {
									sendMessage(channel, String.format("Dvoboj %s - %s, trenutni rezultat -> %s:%s", Colors.smartColoredNick(d.nick1.nick), Colors.smartColoredNick(d.nick2.nick), Colors.paintString(Colors.DARK_GREEN, d.nick1.score), Colors.paintString(Colors.DARK_GREEN, d.nick2.score)));
								}
							}
						}
						break;
					} else {
						lastHint.merge(message.trim());
					}
				}
			} finally {
				playerRepository.saveAndFlush(player);
			}
		}
		return false;
	}

	public synchronized void stopQuiz() {
		if (thread != null) {
			thread.interrupt();
		}
		alive = false;
	}

	@Override
	public void run() {
		try {
			MDC.put("quiz.channel", channel);
			thread = Thread.currentThread();
			
			List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(bot.getConfiguration().getServerHostname(), channel);

			if (players.size() >= 1) {
				Collections.sort(players, Player.CMP_BY_SPEED_ASC);
				channelFastestTime = players.get(0).getFastestTime();
				Player p = players.get(0);
				if (p.getFastestTime() != 0) {
					this.fastestPlayer = p.getId();
				}
				Collections.sort(players, Player.CMP_BY_STREAK_ASC);
				p = players.get(0);
				if (p.getRowRecord() != 0) {
					this.maxStreakPlayer = p.getId();
				}
			}
			log.info("Fastest player: " + fastestPlayer);
			log.info("Max streak player: " + maxStreakPlayer);
			
			reloadQuestions();

			alive = true;
			while (alive && bot.isConnected()) {
				jump = false;
				if (nextId > questions.size()) nextId = 0;
				Question q = null;
				if (nextId > 0) q = questionRepository.findOne((long)nextId);
				synchronized (questions) {
					if (q == null)
						q = questions.get(random.nextInt(questions.size()));
				}
				nextId = 0;
				if (q != null) {
					String answer = q.getAnswers().get(0);
					answer = answer.trim();
					lastHint = new Hint(answer);

					lastQuestion = q;
					log.info("Selected: " + q);
					answered = false;
					long users = getUsers(channel).size() - 1;
					double factor = users * 1.1;
					points = Math.round(lastHint.getAnswerPoints() * factor);
					int b = random.nextInt(quizListener.BONUS_RANDOM_CFG);
					boolean bonus = false;
					if (b == quizListener.BONUS_MATCH_CFG && lastHint.answer.length() < quizListener.BONUS_ON_LENGTH_CFG) {
						bonus = true;
						points *= quizListener.BONUS_FACTORS[random.nextInt(quizListener.BONUS_FACTORS.length)];
					}
					sendMessage(channel, String.format(QUESTION_FORMAT, q.getId(), q.getTheme(), q.getQuestion()));
					sendMessage(channel, String.format(lastHint.answer.length() > quizListener.BONUS_ON_LENGTH_CFG ? HINT1_CHALLENGE_FORMAT : (bonus ? HINT1_BONUS_FORMAT : HINT1_FORMAT), lastHint.getLevel1Hint(), points, getPointsString(points)));
					tstart = System.currentTimeMillis();
					synchronized (this) {
						wait(TimeUnit.SECONDS.toMillis(quizListener.HINT_DELAY_SEC_CFG));
						if (!answered) {
							if (jump) {
								jump = false;
								lastPlayer = 0;
								streak = 1;
								lastAnswer = answer;
								log.debug("Question skipped..");
								continue;
							}
							points = Math.round(points / 2d);
							sendMessage(channel, String.format(HINT2_FORMAT, lastHint.getLevel2Hint(), points, getPointsString(points)));
							wait(TimeUnit.SECONDS.toMillis(quizListener.HINT_DELAY_SEC_CFG));
							if (!answered) {
								if (jump) {
									jump = false;
									lastPlayer = 0;
									streak = 1;
									lastAnswer = answer;
									log.debug("Question skipped..");
									continue;
								}
								points = Math.round(points / 2d);
								sendMessage(channel, String.format(HINT3_FORMAT, lastHint.getLevel3Hint(), points, getPointsString(points)));
								wait(TimeUnit.SECONDS.toMillis(quizListener.HINT_DELAY_SEC_CFG));
							} else {
								answered = true;
							}
						} else {
							answered = true;
						}
						lastQuestion = null;
						if (!answered) {
							sendMessage(channel, TIMEUP_MESSAGE_FORMAT);// Odgovor
							lastPlayer = 0;
							streak = 1;
						}
					}
					lastAnswer = answer;
					Thread.sleep(TimeUnit.SECONDS.toMillis(quizListener.QUESTION_DELAY_SEC_CFG));
				}

				log.info("Question finished.. answered: " + answered);
			}
			sendMessage(channel, "Quiz is stopped.");
		} catch (Throwable t) {
			if (!(t instanceof InterruptedException)) {
				log.error("Unhandled exception in question runnable.", t);
			}
		} finally {
			log.debug("I'm done..");
			quizListener.stopQuiz(bot, channel);
		}
	}

	@SuppressWarnings("incomplete-switch")
	public synchronized void handleCommand(GenericMessageEvent<PircBotX> event, QuizListener.QuizCommand command, User user, String[] arguments) {
		List<String> args = Arrays.asList(arguments);
		switch (command) {
		case H:
			if (!answered && lastQuestion != null) {
				sendH();
			}
			break;
		case V:
			if (!answered && lastQuestion != null) {
				sendV();
			}
			break;
		case Z:
			if (!answered && lastQuestion != null) {
				sendZ();
			}
			break;
		case PONOVI: {
			if (!answered && lastQuestion != null) {
				repeatLastQuestion();
			}
			break;
		}
		case ODG:
		case ODGOVOR:
			if (lastAnswer != null) {
				event.respond(String.format(LAST_ANSWER_FORMAT, lastAnswer));
			}
			break;
		case SCORE:
		case SVEO: {
			String nick = user.getNick();
			if (args != null && args.size() >= 1) {
				nick = args.get(0);
			}
			List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(event.getBot().getConfiguration().getServerHostname(), channel);
			Player player = null;
			for (Player p : players) {
				if (p.getNick().equalsIgnoreCase(nick)) {
					player = p;
					break;
				}
			}
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
				event.respond(
						String.format(JOIN_STATS_FORMAT, Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
			} else {
				event.respond("Ne postoje podaci o igracu " + Colors.smartColoredNick(nick) + " za kanal " + Colors.paintBoldString(4, channel));
			}
			break;
		}
		case JUMP:
		case PRESKOCI:
		case SKIP:
			jump();
			break;
		case SETNEXT:
			int id = 0;
			if (args != null && args.size() >= 1) {
				id = Util.parseInt(args.get(0), 0);
			}
			if (id != 0) {
				nextId = id;
			}
			break;
		case DATEVIDIM: {
			if (event instanceof MessageEvent) {
				MessageEvent<PircBotX> e = (MessageEvent<PircBotX>) event;
				if (args != null && args.size() >= 1) {
					String nick2 = args.get(0);
					int questions = (args.size() == 2 ? Util.parseInt(args.get(1), 10) : 10);
					org.pircbotx.User user2 = getUser(e.getChannel(), nick2);
					if (user2 == null) {
						event.getBot().sendIRC().message(user.getNick(), String.format("Igrac s nadimkom %s nije pronadjen na kanalu %s", Colors.smartColoredNick(nick2), Colors.paintString(Colors.RED, channel)));
					} else if (user2.getNick().equalsIgnoreCase(user.getNick())) {
						event.getBot().sendIRC().message(user.getNick(), "Pokusavate izazvati sami sebe na dvoboj?!");
					} else if (user2.getNick().equalsIgnoreCase(event.getBot().getUserBot().getNick())) {
						event.getBot().sendIRC().message(user.getNick(), "Ne igraj se s vatrom!");
					} else {

						synchronized (duels) {
							Duel hasDuel = null;
							for (Duel d : duels) {
								if ((d.nick1.nick.equalsIgnoreCase(user.getNick()) && d.nick2.nick.equalsIgnoreCase(nick2)) || (d.nick2.nick.equalsIgnoreCase(user.getNick()) && d.nick1.nick.equalsIgnoreCase(nick2))) {
									hasDuel = d;
									break;
								}
							}
							if (hasDuel == null) {
								duels.add(new Duel(user.getNick(), user2.getNick(), questions));
								event.getBot().sendIRC().message(nick2, String.format("%s vas je izazva[o|la] na dvoboj do %s. Potvrdite sa %s, odbijte sa %s.", Colors.smartColoredNick(user.getNick()), Colors.paintString(Colors.RED, questions), Colors.paintString(Colors.DARK_GREEN, quizListener.COMMAND_PREFIX_CFG + QuizCommand.MRTAVSI), Colors.paintString(Colors.BLUE, quizListener.COMMAND_PREFIX_CFG + QuizCommand.ODBIJ)));
							} else {
								if (!hasDuel.confirmed)
									event.getBot().sendIRC().message(user.getNick(), String.format("Vec ste izazvali %s na dvoboj!", Colors.smartColoredNick(nick2)));
								else
									event.getBot().sendIRC().message(user.getNick(), String.format("Vec ste u dvoboju s %s!", Colors.smartColoredNick(nick2)));
							}
						}
					}
				}
			}

			break;
		}
		case MRTAVSI:
		case MRTVASI: {
			String challenger = null;
			if (args!= null && args.size() == 1) {
				challenger = args.get(0);
			}
			synchronized (duels) {
				Duel found = null;
				for (Duel d : duels) {
					if (!d.confirmed && d.nick2.nick.equalsIgnoreCase(user.getNick()) && (challenger != null ? d.nick1.nick.equalsIgnoreCase(challenger) : true)) {
						found = d;
						break;
					}
				}
				if (found == null) {
					if (challenger == null) event.getBot().sendIRC().message(user.getNick(), "Nemate dvoboja koji cekaju potvrdu!");
					else event.getBot().sendIRC().message(user.getNick(), String.format("Niste izazvani od %s!", Colors.smartColoredNick(challenger)));
				} else {
					found.setConfirmed(true);

					Player p1 = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(event.getBot().getConfiguration().getServerHostname(), channel, found.nick1.nick);
					if (p1 != null) {
						p1.incrementDuels();
						p1 = playerRepository.save(p1);
					}
					Player p2 = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(event.getBot().getConfiguration().getServerHostname(), channel, found.nick2.nick);
					if (p2 != null) {
						p2.incrementDuels();
						p2 = playerRepository.save(p2);
					}
					event.getBot().sendIRC().message(channel, String.format("Od iduceg pitanja krece dvoboj do %s izmedju %s i %s!", Colors.paintString(Colors.RED, found.questions), Colors.smartColoredNick(found.nick1.nick), Colors.smartColoredNick(found.nick2.nick)));
				}
			}

			break;
		}
		case NECU:
		case ODBIJ: {
			synchronized (duels) {
				String challenger = null;
				if (args!= null && args.size() == 1) {
					challenger = args.get(0);
				}
				Duel found = null;
				for (Duel d : duels) {
					if (!d.confirmed && d.nick2.nick.equalsIgnoreCase(user.getNick()) && (challenger != null ? d.nick1.nick.equalsIgnoreCase(challenger) : true)) {
						found = d;
						break;
					}
				}
				if (found == null) {
					if (challenger == null) event.getBot().sendIRC().message(user.getNick(), "Nemate dvoboja koji cekaju potvrdu!");
					else event.getBot().sendIRC().message(user.getNick(), String.format("Niste izazvani od %s!", Colors.smartColoredNick(challenger)));
				} else {
					event.getBot().sendIRC().message(found.nick1.nick, Colors.smartColoredNick(found.nick2.nick)+ " je odbi[o|la] poziv na dvoboj.");
					duels.remove(found);
				}
			}
			break;
		}
		case SVIDVOBOJI: {
			if (duels.isEmpty()) {
				event.getBot().sendIRC().message(user.getNick(), "Nema aktivnih dvoboja.");
			} else {
				synchronized (duels) {
					event.getBot().sendIRC().message(user.getNick(), "Dvoboji na kanalu " + channel + ":");
					for (Duel d : duels) {
						event.getBot().sendIRC().message(user.getNick(), d.toString());
					}
				}
			}
			break;
		}
		case RELOAD: {
			reloadQuestions();
			break;
		}
		case FANATICI: {
			String category = "score";
			if (args != null && args.size() >= 1) {
				category = args.get(0);
				if (args.size() == 2) {
					category = args.get(0) + " " + args.get(1);
				}
			} else {
				event.getBot().sendIRC().message(user.getNick(), "Dostupne kategorije za {C}4" + command + "{C}: {C}12score, month, week, row, speed, duels, duels won{C}");
			}
			List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(event.getBot().getConfiguration().getServerHostname(), channel);

			if (players.isEmpty()) {
				StringBuilder response = new StringBuilder();
				response.append(String.format("Nema bodovne liste igraca (server:%s, kanal:%s)!", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel)));
				event.respond(response.toString());
				break;
			}

			if (category.equalsIgnoreCase("score")) {
				Collections.sort(players, Player.CMP_BY_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("month")) {
				Collections.sort(players, Player.CMP_BY_MONTH_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getMonthScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("week")) {
				Collections.sort(players, Player.CMP_BY_WEEK_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getWeekScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("row")) {
				Collections.sort(players, Player.CMP_BY_STREAK_ASC);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getRowRecord() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("speed")) {
				Collections.sort(players, Player.CMP_BY_SPEED_ASC);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getFastestTime() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("duels")) {
				Collections.sort(players, Player.CMP_BY_DUELS);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getDuels() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("duels won")) {
				Collections.sort(players, Player.CMP_BY_DUELS_WON);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getDuelsWon() == 0) {
						iterator.remove();
					}
				}
			} else {
				category = "score";
				Collections.sort(players, Player.CMP_BY_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getScore() == 0) {
						iterator.remove();
					}
				}
			}

			if (players.isEmpty()) {
				StringBuilder response = new StringBuilder();
				response.append(String.format("Nema fanatika (server:%s, kanal:%s) u kategoriji:%s!", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
				event.respond(response.toString());
				break;
			}

			List<Player> all = new ArrayList<Player>(players);

			StringBuilder response = new StringBuilder();
			response.append(String.format("Fanatici (server:%s, kanal:%s) u kategoriji:%s\n", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
			int i = 1;
			for (Player player : players) {
				Collections.sort(all, Player.CMP_BY_SCORE);
				int scorePos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_MONTH_SCORE);
				int monthPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_WEEK_SCORE);
				int weekPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_SPEED_ASC);
				int speedPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_STREAK_ASC);
				int streekPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_DUELS);
				int duelsPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_DUELS_WON);
				int duelsWonPos = all.indexOf(player) + 1;
				response.append(Colors.paintBoldString(Colors.BLUE, "#" + i + " ") + String.format(JOIN_STATS_FORMAT + "\n", Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
				if (i == 3) break;
				i++;
			}
			event.respond(response.toString());
			break;
		}
		case TOP10:
			String category = "score";
			if (args != null && args.size() >= 1) {
				category = args.get(0);
				if (args.size() == 2) {
					category = args.get(0) + " " + args.get(1);
				}
			} else {
				event.respond("Dostupne kategorije za {C}4" + command + "{C}: {C}12score, month, week, row, speed, duels, duels won{C}");
			}
			List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(event.getBot().getConfiguration().getServerHostname(), channel);

			if (players.isEmpty()) {
				StringBuilder response = new StringBuilder();
				response.append(String.format("Nema bodovne liste igraca (server:%s, kanal:%s)!", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel)));
				event.respond(response.toString());
				break;
			}

			if (category.equalsIgnoreCase("score")) {
				Collections.sort(players, Player.CMP_BY_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("month")) {
				Collections.sort(players, Player.CMP_BY_MONTH_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getMonthScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("week")) {
				Collections.sort(players, Player.CMP_BY_WEEK_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getWeekScore() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("row")) {
				Collections.sort(players, Player.CMP_BY_STREAK_ASC);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getRowRecord() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("speed")) {
				Collections.sort(players, Player.CMP_BY_SPEED_ASC);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getFastestTime() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("duels")) {
				Collections.sort(players, Player.CMP_BY_DUELS);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getDuels() == 0) {
						iterator.remove();
					}
				}
			} else if (category.equalsIgnoreCase("duels won")) {
				Collections.sort(players, Player.CMP_BY_DUELS_WON);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getDuelsWon() == 0) {
						iterator.remove();
					}
				}
			} else {
				category = "score";
				Collections.sort(players, Player.CMP_BY_SCORE);
				for (Iterator<Player> iterator = players.iterator(); iterator.hasNext();) {
					if (iterator.next().getScore() == 0) {
						iterator.remove();
					}
				}
			}

			if (players.isEmpty()) {
				StringBuilder response = new StringBuilder();
				response.append(String.format("Nema TOP10 igraca (server:%s, kanal:%s) u kategoriji:%s!", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
				event.respond(response.toString());
				break;
			}

			List<Player> all = new ArrayList<Player>(players);

			StringBuilder response = new StringBuilder();
			response.append(String.format("TOP10 igraca (server:%s, kanal:%s) u kategoriji:%s\n", Colors.paintString(Colors.BLUE, event.getBot().getConfiguration().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
			int i = 1;
			for (Player player : players) {
				Collections.sort(all, Player.CMP_BY_SCORE);
				int scorePos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_MONTH_SCORE);
				int monthPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_WEEK_SCORE);
				int weekPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_SPEED_ASC);
				int speedPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_STREAK_ASC);
				int streekPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_DUELS);
				int duelsPos = all.indexOf(player) + 1;
				Collections.sort(all, Player.CMP_BY_DUELS_WON);
				int duelsWonPos = all.indexOf(player) + 1;
				response.append(Colors.paintBoldString(Colors.BLUE, "#" + i + " ") + String.format(JOIN_STATS_FORMAT +"\n", Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
				if (i == 10) break;
				i++;
			}
			event.respond(response.toString());
			break;
		}
	}

	public Duel findDuel(String nick) {
		synchronized (duels) {
			for (Duel d : duels) {
				if (d.nick1.nick.equalsIgnoreCase(nick)) {
					return d;
				}
				if (d.nick2.nick.equalsIgnoreCase(nick)) {
					return d;
				}
			}
			return null;
		}
	}

	public List<Duel> findDuels(String nick) {
		synchronized (duels) {
			List<Duel> _duels = new ArrayList<Duel>();
			for (Duel d : duels) {
				if (d.nick1.nick.equalsIgnoreCase(nick)) {
					_duels.add(d);
				} else if (d.nick2.nick.equalsIgnoreCase(nick)) {
					_duels.add(d);
				}
			}
			return _duels;
		}
	}
	
	public void clean() {
		cleanDuels();
		checkInactivity();
	}

	private void cleanDuels() {
		synchronized (duels) {
			if (duels.isEmpty()) return;
			for (Iterator<Duel> iterator = duels.iterator(); iterator.hasNext();) {
				Duel duel = iterator.next();
				if ((System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(QuizListener.DUEL_ACTIVITY_TIMEOUT_CFG)) > duel.lastActivity) {
					iterator.remove();
				}
			}
		}
	}
	
	public List<Player> cleanScore() {
		log.debug("Resetting score...");
		List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(bot.getConfiguration().getServerHostname(), channel);
		log.info("Loaded {} players", players.size());
		int cnt = 0;
		try {
			for (Player p : players) {
				if (p.resetScore()) {
					cnt++;
				}
			}
			players = playerRepository.save(players);
		} catch (Throwable t) {
			log.error("Error resetting score", t);
		} finally {
			log.debug("Reseted score of {} players.", cnt);
		}
		return players;
	}

	private void checkInactivity() {
		if (quizListener.CHANNEL_TIMEOUT_MINUTES_CFG > 0) {
			if (lastActivity < (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(quizListener.CHANNEL_TIMEOUT_MINUTES_CFG))) {
				log.debug("Stopping quiz due to inactivity. Last activity on channel was at: " + Util.formatDate(new Date(lastActivity), null));
				sendMessage(channel, "Zaustavljam kviz.. Nitko se ne zeli igrati :(");
				stopQuiz();
			}
		}
	}

	public long getFastestPlayer() {
		return fastestPlayer;
	}

	public void setFastestPlayer(long fastestPlayer) {
		this.fastestPlayer = fastestPlayer;
	}

	public long getMaxStreakPlayer() {
		return maxStreakPlayer;
	}

	public void setMaxStreakPlayer(long maxStreakPlayer) {
		this.maxStreakPlayer = maxStreakPlayer;
	}
	
	static class Duel {

		PlayerDuelScore nick1;
		PlayerDuelScore nick2;
		int questions;
		boolean confirmed;
		long initiatedAt;
		long startedAt;
		long lastActivity;

		public Duel(String nick1, String nick2, int questions) {
			this.nick1 = new PlayerDuelScore(nick1);
			this.nick2 = new PlayerDuelScore(nick2);
			this.questions = questions;
			this.initiatedAt = System.currentTimeMillis();
			this.lastActivity = this.initiatedAt;
			confirmed = false;
		}

		public void setLastActivity(long lastActivity) {
			this.lastActivity = lastActivity;
		}

		public void setConfirmed(boolean confirmed) {
			this.confirmed = confirmed;
			if (confirmed) {
				this.startedAt = System.currentTimeMillis();
				this.lastActivity = this.startedAt;
			}
		}

		public PlayerDuelScore getDuelist(String nick) {
			if (nick1.nick.equalsIgnoreCase(nick)) return nick1;
			if (nick2.nick.equalsIgnoreCase(nick)) return nick2;
			return null;
		}

		@Override
		public String toString() {
			return "Dvoboj - " + (confirmed  ? "startan: " + new Date(startedAt) : "iniciran: " + new Date(initiatedAt)) + ", broj pitanja: " + questions + ", stanje: " + this.nick1.nick + " (" + this.nick1.score + ") :" + this.nick2.nick + " (" + this.nick2.score + ")";
		}

		boolean isFinished() {
			return nick1.score >= questions || nick2.score >= questions;
		}

		static class PlayerDuelScore {
			String nick;
			int score;

			PlayerDuelScore(String nick) {
				this.nick = nick;
			}
		}
	}
}
