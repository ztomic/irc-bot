package com.ztomic.ircbot.listener.quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ztomic.ircbot.configuration.Formats;
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
import com.ztomic.ircbot.util.TimeUtil;
import com.ztomic.ircbot.util.Util;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


public class QuizChannelHandler implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(QuizChannelHandler.class);

	private static String ZERO_RATED_REGEX = "";

	static {
		for (char c : QuizListener.ZERRO_RATED_CHARS) {
			ZERO_RATED_REGEX += c;
		}
		ZERO_RATED_REGEX = "[" + Pattern.quote(ZERO_RATED_REGEX.trim()) + "]";
	}

	private final QuizSettings quizSettings;
	private final QuestionRepository questionRepository;
	private final PlayerRepository playerRepository;
	private final UserRepository userRepository;
	private final MessagesConfiguration messagesConfiguration;
	
	private QuizMessages quizMessages;

	private final PircBotX bot;
	private final QuizListener quizListener;
	private final String channel;
	private final String language;
	
	private Question currentQuestion;
	private long lastActivity = System.currentTimeMillis();
	private String previousQuestionAnswer;
	private Hint lastHint;
	private long lastPlayer;
	private int streak = 1;
	private boolean answered = false;
	private long points;
	private long tstart;
	boolean alive;
	
	private List<Question> questions;
	private int nextId = 0;
	private static final Random random = new Random();
	private boolean jump;
	
	private long channelFastestTime = 0;
	private long fastestPlayer;
	private long maxStreakPlayer;

	private final List<Duel> duels = new ArrayList<>();
	
	private Thread thread;

	public QuizChannelHandler(QuizSettings quizSettings, QuestionRepository questionRepository, PlayerRepository playerRepository, UserRepository userRepository, MessagesConfiguration messagesConfiguration, PircBotX bot, QuizListener quizHandler, String channel, String language) {
		this.quizSettings = quizSettings;
		this.questionRepository = questionRepository;
		this.playerRepository = playerRepository;
		this.userRepository = userRepository;
		this.messagesConfiguration = messagesConfiguration;
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
		if (quizMessages == null) {
			quizMessages = messagesConfiguration.getQuizMessages(language);
		}
		return quizMessages;
	}
	
	Formats getFormats() {
		return getQuizMessages().getFormats();
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
			sendMessage(channel, String.format(getFormats().getFirstCharFormat(), h));
		}
	}

	public void sendV() {
		String v = lastHint.getVowels();
		if (v != null) {
			sendMessage(channel, String.format(getFormats().getVowelsFormat(), v));
		}
	}

	public void sendZ() {
		String z = lastHint.getLastChar();
		if (z != null) {
			sendMessage(channel, String.format(getFormats().getLastCharFormat(), z));
		}
	}

	public synchronized void repeatLastQuestion() {
		if (currentQuestion != null && !answered) {
			sendMessage(channel, String.format(getFormats().getQuestionFormat(), currentQuestion.getId(), currentQuestion.getTheme(), currentQuestion.getQuestion()));
		}
	}

	public class Hint {

		private final String answer;
		private final double answerPoints;

		private boolean sentH;
		private boolean sentV;
		private boolean sentZ;

		private int phase = 1;

		private final StringBuilder hint;

		Hint(String answer) {
			this.answer = answer;
			hint = new StringBuilder();
			for (char c : answer.toCharArray()) {
				if (Arrays.binarySearch(QuizListener.ZERRO_RATED_CHARS, c) >= 0) {
					hint.append(c);
				} else {
					hint.append(quizSettings.getHintChar());
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
				if (i == 0) {
					pt += quizSettings.getFirstCharPoints();
					continue;
				}
				if (i == answer.length() - 1) {
					pt += quizSettings.getLastCharPoints();
					continue;
				}
				if (Arrays.binarySearch(QuizListener.ZERRO_RATED_CHARS, c) >= 0) {
					continue;
				}
				if (Character.isDigit(c)) {
					pt += quizSettings.getNumberPoints();
					continue;
				}
				if (Arrays.binarySearch(QuizListener.VOWELS, c) >= 0) {
					pt += quizSettings.getVowelPoints();
					continue;
				}
				pt += quizSettings.getLetterPoints();
			}
			if (answer.length() > quizSettings.getBonusOnLength()) {
				pt += (answer.length() * quizSettings.getBonusOnLengthFactor());
			}
			return pt;
		}

		long getAnswerPoints() {
			return Math.round(answerPoints);
		}

		synchronized String getVowels() {
			if (!sentV) {
				StringBuilder vowels = new StringBuilder();
				for (int i = 0; i < answer.length(); i++) {
					char chr = answer.charAt(i);
					if (Arrays.binarySearch(QuizListener.VOWELS, chr) >= 0 || chr == ' ') {
						vowels.append(chr);
						if (hint.charAt(i) == quizSettings.getHintChar()) {
							hint.replace(i, i + 1, String.valueOf(chr));
						}
					} else {
						vowels.append(quizSettings.getHintChar());
					}
				}
				sentV = true;
				return vowels.toString();
			}
			return null;
		}

		synchronized String getFirstChar() {
			if (!sentH) {
				if (hint.charAt(0) == quizSettings.getHintChar()) {
					hint.replace(0, 1, String.valueOf(answer.charAt(0)));
				}
				sentH = true;
				return String.valueOf(hint.charAt(0));
			}
			return null;
		}

		synchronized String getLastChar() {
			if (!sentZ) {
				if (hint.charAt(hint.length() - 1) == quizSettings.getHintChar()) {
					hint.replace(hint.length() - 1, hint.length(), String.valueOf(answer.charAt(answer.length() - 1)));
				}
				sentZ = true;
				return String.valueOf(hint.charAt(hint.length() - 1));
			}
			return null;
		}

		synchronized String getLevel3Hint() {
			phase = 3;
			for (int i = 0; i < answer.length(); i++) {
				if (i % 2 == 0 || answer.charAt(i) == ' ') {
					if (hint.charAt(i) == quizSettings.getHintChar()) {
						hint.replace(i, i + 1, String.valueOf(answer.charAt(i)));
					}
				}
			}
			return hint.toString();
		}

		synchronized String getLevel2Hint() {
			phase = 2;
			getFirstChar();
			return hint.toString();
		}

		synchronized String getLevel1Hint() {
			phase = 1;
			return hint.toString();
		}

		synchronized void merge(String text) {
			String oldHint = hint.toString();
			text = text.toLowerCase();
			for (int i = 0; i < text.length(); i++) {
				if (i >= answer.length()) {
					break;
				}
				char c = text.charAt(i);
				if (answer.toLowerCase().charAt(i) == c) {
					if (hint.charAt(i) == quizSettings.getHintChar()) {
						hint.replace(i, i + 1, String.valueOf(answer.charAt(i)));
					}
				}

			}
			if (quizSettings.isShowGuessed() && !oldHint.contentEquals(hint) && phase >= quizSettings.getShowGuessedPhase()) {
				sendMessage(channel, String.format(getFormats().getGuessedFormat(), hint));
			}
		}
	}

	private static String getPointsString(long i) {
		if (i < 0) {
			i = -i;
		}
		i %= 100;
		if (i >= 10 && i <= 20) {
			return "bodova";
		}
		i %= 10;
		return ((i >= 5 || i == 0) ? "bodova" : (i >= 2 ? "boda" : "bod"));
	}

	private synchronized void reloadQuestions() {
		this.questions = questionRepository.findByLanguageIgnoreCase(language);
		log.info("Reloaded {} questions.", questions.size());
	}

	private boolean patternMatches(String answer, String message) {
		try {
			return answer.trim().matches(message);
		} catch (PatternSyntaxException e) {
			return false;
		}
	}

	private boolean isAnswer(String answer, String message) {
		boolean match = answer.trim().equalsIgnoreCase(message);
		if (!match) {
			match = answer.replaceAll(ZERO_RATED_REGEX, "").trim().equalsIgnoreCase(message);
		}

		return match;
	}

	synchronized boolean handle(GenericMessageEvent msg) {
		lastActivity = System.currentTimeMillis();
		String message = msg.getMessage();
		long time = (System.currentTimeMillis() - tstart);
		if (currentQuestion != null && !answered) {
			Player player = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(msg.getBot().getServerHostname(), channel, msg.getUser().getNick());

			if (player == null) {
				player = new Player();
				player.setChannel(channel);
				player.setNick(msg.getUser().getNick());
				player.setScore(0);
				player.setServer(msg.getBot().getServerHostname());
				player = playerRepository.saveAndFlush(player);
			}

			try {
				for (String answer : currentQuestion.getAnswers()) {
					User user = userRepository.findByServerAndNickIgnoreCase(player.getServer(), player.getNick());
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
								Colors.paintString(Colors.BLUE, comment + ",") + Colors.smartColoredNick(player.getNick()) + Colors.paintString(Colors.BLUE, "! Odgovor je ->") + Colors.paintBoldString(Colors.DARK_BLUE, Colors.YELLOW, " " + answer + " ") + Colors.paintString(Colors.BLUE, "<-. Vrijeme:") + Colors.paintString(Colors.DARK_GREEN, (time / 1000F)) + Colors.paintString(Colors.BLUE, "sec") + (record ? " (" + Colors.paintString(Colors.WHITE, Colors.RED, "OSOBNI REKORD!") + ")." : ".") + (hasStreak ? Colors.paintString(Colors.BLUE, "Niz:") + Colors.paintString(Colors.DARK_GREEN, streak) + Colors.paintString(Colors.BLUE, ".") : "") + Colors.paintString(Colors.BLUE, "Dobivate") + Colors.paintString(Colors.DARK_GREEN, points)
								+ Colors.paintString(Colors.BLUE, getPointsString(points) + ".") + Colors.paintString(Colors.BLUE, "Novi score:") + Colors.paintString(Colors.DARK_GREEN, player.getScore()));
						if (channelFastestTime == 0 || channelFastestTime > time) {
							Player fastestPlayer = getFastestPlayer() > 0 ? playerRepository.findById(getFastestPlayer()).orElse(null) : null;
							sendMessage(channel, Colors.smartColoredNick(player.getNick()) + " je obori[o|la] brzinski rekord kanala" + (fastestPlayer != null ? " koji je do sada drza[o|la] " + Colors.smartColoredNick(fastestPlayer.getNick()) + " sa " + Colors.paintString(Colors.DARK_GREEN, fastestPlayer.getFastestTime() / 1000F) + "sec" : "."));
							channelFastestTime = time;
							setFastestPlayer(player.getId());
						}
						if (streak > player.getRowRecord() && hasStreak) {
							player.setRowRecord(streak);
							//sendMessage(player.nick, String.format(PERSONAL_STREAK_RECORD_COMMENT_FORMAT, player.rowRecord));
						}
						if ((getMaxStreakPlayer() <= 0 || streak > playerRepository.findById(getMaxStreakPlayer()).map(Player::getRowRecord).orElse(0)) && hasStreak) {
							setMaxStreakPlayer(player.getId());
							sendMessage(channel, String.format(getFormats().getChannelStreakRecordCommentFormat(), player.getNick(), player.getRowRecord()));
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
									sendMessage(channel, String.format("Dvoboj izmedju %s i %s je zavrsen (%s:%s), Cestitamo %s!", Colors.smartColoredNick(d.firstPlayer.nick), Colors.smartColoredNick(d.secondPlayer.nick), Colors.paintString(Colors.DARK_GREEN, d.firstPlayer.score), Colors.paintString(Colors.DARK_GREEN, d.secondPlayer.score), Colors.smartColoredNick(player.getNick())));
								} else {
									sendMessage(channel, String.format("Dvoboj %s - %s, trenutni rezultat -> %s:%s", Colors.smartColoredNick(d.firstPlayer.nick), Colors.smartColoredNick(d.secondPlayer.nick), Colors.paintString(Colors.DARK_GREEN, d.firstPlayer.score), Colors.paintString(Colors.DARK_GREEN, d.secondPlayer.score)));
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

	synchronized void stopQuiz() {
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
			
			List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(bot.getServerHostname(), channel);

			if (players.size() >= 1) {
				players.sort(Player.CMP_BY_SPEED_ASC);
				channelFastestTime = players.getFirst().getFastestTime();
				Player p = players.getFirst();
				if (p.getFastestTime() != 0) {
					this.fastestPlayer = p.getId();
				}
				players.sort(Player.CMP_BY_STREAK_ASC);
				p = players.getFirst();
				if (p.getRowRecord() != 0) {
					this.maxStreakPlayer = p.getId();
				}
			}
			log.info("Fastest player: {}", fastestPlayer);
			log.info("Max streak player: {}", maxStreakPlayer);
			
			reloadQuestions();

			alive = true;
			while (alive && bot.isConnected()) {
				jump = false;
				if (nextId > questions.size()) {
					nextId = 0;
				}
				Question q = null;
				if (nextId > 0) {
					q = questionRepository.findById((long)nextId).orElse(null);
				}
				synchronized (questions) {
					if (q == null) {
						q = questions.get(random.nextInt(questions.size()));
					}
				}
				nextId = 0;
				if (q != null) {
					String answer = q.getAnswers().getFirst();
					answer = answer.trim();
					lastHint = new Hint(answer);

					currentQuestion = q;
					log.info("Selected: {}", q);
					answered = false;
					long users = getUsers(channel).size() - 1;
					double factor = users * 1.1;
					points = Math.round(lastHint.getAnswerPoints() * factor);
					int b = random.nextInt(quizSettings.getBonusRandom());
					boolean bonus = false;
					if (b == quizSettings.getBonusMatch() && lastHint.answer.length() < quizSettings.getBonusOnLength()) {
						bonus = true;
						points *= quizListener.BONUS_FACTORS[random.nextInt(quizListener.BONUS_FACTORS.length)];
					}
					sendMessage(channel, String.format(getFormats().getQuestionFormat(), q.getId(), q.getTheme(), q.getQuestion()));
					sendMessage(channel, String.format(lastHint.answer.length() > quizSettings.getBonusOnLength() ? getFormats().getHint1ChallengeFormat() : (bonus ? getFormats().getHint1BonusFormat() : getFormats().getHint1Format()), lastHint.getLevel1Hint(), points, getPointsString(points)));
					tstart = System.currentTimeMillis();
					synchronized (this) {
						wait(TimeUnit.SECONDS.toMillis(quizSettings.getHintDelaySec()));
						if (!answered) {
							if (jump) {
								jump = false;
								lastPlayer = 0;
								streak = 1;
								previousQuestionAnswer = answer;
								log.debug("Question skipped..");
								continue;
							}
							points = Math.round(points / 2d);
							sendMessage(channel, String.format(getFormats().getHint2Format(), lastHint.getLevel2Hint(), points, getPointsString(points)));
							wait(TimeUnit.SECONDS.toMillis(quizSettings.getHintDelaySec()));
							if (!answered) {
								if (jump) {
									jump = false;
									lastPlayer = 0;
									streak = 1;
									previousQuestionAnswer = answer;
									log.debug("Question skipped..");
									continue;
								}
								points = Math.round(points / 2d);
								sendMessage(channel, String.format(getFormats().getHint3Format(), lastHint.getLevel3Hint(), points, getPointsString(points)));
								wait(TimeUnit.SECONDS.toMillis(quizSettings.getHintDelaySec()));
							}
						}
						currentQuestion = null;
						if (!answered) {
							sendMessage(channel, getFormats().getTimeupMessageFormat());// Odgovor
							lastPlayer = 0;
							streak = 1;
						}
					}
					previousQuestionAnswer = answer;
					Thread.sleep(TimeUnit.SECONDS.toMillis(quizSettings.getQuestionDelaySec()));
				}

				log.info("Question finished with{} answer.", answered ? "" : "out");
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
	protected synchronized void handleCommand(GenericMessageEvent event, QuizListener.QuizCommand command, User user, String[] arguments) {
		List<String> args = Arrays.asList(arguments);
		switch (command) {
			case H -> {
				if (!answered && currentQuestion != null) {
					sendH();
				}
			}
			case V -> {
				if (!answered && currentQuestion != null) {
					sendV();
				}
			}
			case Z -> {
				if (!answered && currentQuestion != null) {
					sendZ();
				}
			}
			case PONOVI -> {
				if (!answered && currentQuestion != null) {
					repeatLastQuestion();
				}
			}
			case ODG, ODGOVOR -> {
				if (previousQuestionAnswer != null) {
					event.respond(String.format(getFormats().getLastAnswerFormat(), previousQuestionAnswer));
				}
			}
			case SCORE, SVEO -> {
				String nick = user.getNick();
				if (args.size() >= 1) {
					nick = args.getFirst();
				}
				List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(event.getBot().getServerHostname(), channel);
				Player player = null;
				for (Player p : players) {
					if (p.getNick().equalsIgnoreCase(nick)) {
						player = p;
						break;
					}
				}
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
					event.respond(
							String.format(getFormats().getJoinStatsFormat(), Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
				} else {
					event.respond("Ne postoje podaci o igracu " + Colors.smartColoredNick(nick) + " za kanal " + Colors.paintBoldString(4, channel));
				}
			}
			case JUMP, PRESKOCI, SKIP -> jump();
			case SETNEXT -> {
				int id = 0;
				if (args.size() >= 1) {
					id = Util.parseInt(args.getFirst(), 0);
				}
				if (id != 0) {
					nextId = id;
				}
			}
			case DATEVIDIM -> {
				if (event instanceof MessageEvent e) {
					if (args.size() >= 1) {
						String nick2 = args.get(0);
						int questions = (args.size() == 2 ? Util.parseInt(args.get(1), 10) : 10);
						org.pircbotx.User user2 = getUser(e.getChannel(), nick2);
						if (user2 == null) {
							event.getBot().sendIRC().message(user.getNick(), String.format("Igrac s nadimkom %s nije pronadjen na kanalu %s", Colors.smartColoredNick(nick2), Colors.paintString(Colors.RED, channel)));
						} else if (user2.getNick().equalsIgnoreCase(user.getNick())) {
							event.getBot().sendIRC().message(user.getNick(), "{C}4Pokusavate izazvati sami sebe na dvoboj?!");
						} else if (user2.getNick().equalsIgnoreCase(event.getBot().getUserBot().getNick())) {
							event.getBot().sendIRC().message(user.getNick(), "{C}4Ne igraj se s vatrom!");
						} else {

							synchronized (duels) {
								Duel hasDuel = null;
								for (Duel d : duels) {
									if ((d.firstPlayer.nick.equalsIgnoreCase(user.getNick()) && d.secondPlayer.nick.equalsIgnoreCase(nick2)) || (d.secondPlayer.nick.equalsIgnoreCase(user.getNick()) && d.firstPlayer.nick.equalsIgnoreCase(nick2))) {
										hasDuel = d;
										break;
									}
								}
								if (hasDuel == null) {
									duels.add(new Duel(user.getNick(), user2.getNick(), questions));
									event.getBot().sendIRC().message(nick2, String.format("%s vas je izazva[o|la] na dvoboj do %s. Potvrdite sa %s, odbijte sa %s.", Colors.smartColoredNick(user.getNick()), Colors.paintString(Colors.RED, questions), Colors.paintString(Colors.DARK_GREEN, quizListener.getCommandPrefix() + QuizCommand.MRTAVSI), Colors.paintString(Colors.BLUE, quizListener.getCommandPrefix() + QuizCommand.ODBIJ)));
								} else {
									if (!hasDuel.confirmed) {
										event.getBot().sendIRC().message(user.getNick(), String.format("Vec ste izazvali %s na dvoboj!", Colors.smartColoredNick(nick2)));
									} else {
										event.getBot().sendIRC().message(user.getNick(), String.format("Vec ste u dvoboju s %s!", Colors.smartColoredNick(nick2)));
									}
								}
							}
						}
					}
				}

			}
			case MRTAVSI, MRTVASI -> {
				String challenger = null;
				if (args.size() == 1) {
					challenger = args.getFirst();
				}
				synchronized (duels) {
					Duel found = null;
					for (Duel d : duels) {
						if (!d.confirmed && d.secondPlayer.nick.equalsIgnoreCase(user.getNick()) && (challenger == null || d.firstPlayer.nick.equalsIgnoreCase(challenger))) {
							found = d;
							break;
						}
					}
					if (found == null) {
						if (challenger == null) event.getBot().sendIRC().message(user.getNick(), "{C}4Nemate dvoboja koji cekaju potvrdu!");
						else event.getBot().sendIRC().message(user.getNick(), String.format("Niste izazvani od %s!", Colors.smartColoredNick(challenger)));
					} else {
						found.confirm();

						Player p1 = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(event.getBot().getServerHostname(), channel, found.firstPlayer.nick);
						if (p1 != null) {
							p1.incrementDuels();
							p1 = playerRepository.save(p1);
						}
						Player p2 = playerRepository.findByServerAndChannelIgnoreCaseAndNickIgnoreCase(event.getBot().getServerHostname(), channel, found.secondPlayer.nick);
						if (p2 != null) {
							p2.incrementDuels();
							p2 = playerRepository.save(p2);
						}
						event.getBot().sendIRC().message(channel, String.format("Od iduceg pitanja krece dvoboj do %s izmedju %s i %s!", Colors.paintString(Colors.RED, found.questions), Colors.smartColoredNick(found.firstPlayer.nick), Colors.smartColoredNick(found.secondPlayer.nick)));
					}
				}

			}
			case NECU, ODBIJ -> {
				synchronized (duels) {
					String challenger = null;
					if (args.size() == 1) {
						challenger = args.getFirst();
					}
					Duel found = null;
					for (Duel d : duels) {
						if (!d.confirmed && d.secondPlayer.nick.equalsIgnoreCase(user.getNick()) && (challenger == null || d.firstPlayer.nick.equalsIgnoreCase(challenger))) {
							found = d;
							break;
						}
					}
					if (found == null) {
						if (challenger == null) event.getBot().sendIRC().message(user.getNick(), "{C}4Nemate dvoboja koji cekaju potvrdu!");
						else event.getBot().sendIRC().message(user.getNick(), String.format("Niste izazvani od %s!", Colors.smartColoredNick(challenger)));
					} else {
						event.getBot().sendIRC().message(found.firstPlayer.nick, Colors.smartColoredNick(found.secondPlayer.nick) + " je odbi[o|la] poziv na dvoboj.");
						duels.remove(found);
					}
				}
			}
			case SVIDVOBOJI -> {
				if (duels.isEmpty()) {
					event.getBot().sendIRC().message(user.getNick(), "{C}4Nema aktivnih dvoboja.");
				} else {
					synchronized (duels) {
						event.getBot().sendIRC().message(user.getNick(), "{C}3Dvoboji na kanalu " + channel + ":");
						for (Duel d : duels) {
							event.getBot().sendIRC().message(user.getNick(), d.toString());
						}
					}
				}
			}
			case RELOAD -> {
				reloadQuestions();
			}
			case TOP3, TOP10 -> {
				String category = "score";
				if (args.size() >= 1) {
					category = args.get(0);
					if (args.size() == 2) {
						category = args.get(0) + " " + args.get(1);
					}
				} else {
					event.respond("Dostupne kategorije za {C}4" + command + "{C}: {C}12score, month, week, row, speed, duels, duels won{C}");
				}
				List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(event.getBot().getServerHostname(), channel);
				if (players.isEmpty()) {
					event.respond(String.format("Nema bodovne liste igraca (server:%s, kanal:%s)!", Colors.paintString(Colors.BLUE, event.getBot().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel)));
					break;
				}
				if (category.equalsIgnoreCase("score")) {
					players.sort(Player.CMP_BY_SCORE);
					players.removeIf(player -> player.getScore() == 0);
				} else if (category.equalsIgnoreCase("month")) {
					players.sort(Player.CMP_BY_MONTH_SCORE);
					players.removeIf(player -> player.getMonthScore() == 0);
				} else if (category.equalsIgnoreCase("week")) {
					players.sort(Player.CMP_BY_WEEK_SCORE);
					players.removeIf(player -> player.getWeekScore() == 0);
				} else if (category.equalsIgnoreCase("row")) {
					players.sort(Player.CMP_BY_STREAK_ASC);
					players.removeIf(player -> player.getRowRecord() == 0);
				} else if (category.equalsIgnoreCase("speed")) {
					players.sort(Player.CMP_BY_SPEED_ASC);
					players.removeIf(player -> player.getFastestTime() == 0);
				} else if (category.equalsIgnoreCase("duels")) {
					players.sort(Player.CMP_BY_DUELS);
					players.removeIf(player -> player.getDuels() == 0);
				} else if (category.equalsIgnoreCase("duels won")) {
					players.sort(Player.CMP_BY_DUELS_WON);
					players.removeIf(player -> player.getDuelsWon() == 0);
				} else {
					category = "score";
					players.sort(Player.CMP_BY_SCORE);
					players.removeIf(player -> player.getScore() == 0);
				}
				if (players.isEmpty()) {
					event.respond(String.format("Nema %s igraca (server:%s, kanal:%s) u kategoriji:%s!", command, Colors.paintString(Colors.BLUE, event.getBot().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
					break;
				}
				List<Player> all = new ArrayList<>(players);
				StringBuilder response = new StringBuilder();
				response.append(String.format("%s igraca (server:%s, kanal:%s) u kategoriji:%s\n", command, Colors.paintString(Colors.BLUE, event.getBot().getServerHostname()), Colors.paintString(Colors.DARK_GREEN, channel), Colors.paintString(Colors.RED, category)));
				int i = 1;
				for (Player player : players) {
					all.sort(Player.CMP_BY_SCORE);
					int scorePos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_MONTH_SCORE);
					int monthPos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_WEEK_SCORE);
					int weekPos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_SPEED_ASC);
					int speedPos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_STREAK_ASC);
					int streekPos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_DUELS);
					int duelsPos = all.indexOf(player) + 1;
					all.sort(Player.CMP_BY_DUELS_WON);
					int duelsWonPos = all.indexOf(player) + 1;
					response.append(Colors.paintBoldString(Colors.BLUE, "#" + i + " ")).append(String.format(getFormats().getJoinStatsFormat() + "\n", Colors.smartColoredNick(player.getNick()), player.getScore(), scorePos, player.getMonthScore(), monthPos, player.getWeekScore(), weekPos, player.getFastestTime() / 1000F, speedPos, player.getRowRecord(), streekPos, player.getDuels(), duelsPos, player.getDuelsWon(), duelsWonPos));
					if (command == QuizCommand.TOP3) {
						if (i == 3) {
							break;
						}
					}
					if (command == QuizCommand.TOP10) {
						if (i == 10) {
							break;
						}
					}
					i++;
				}
				event.respond(response.toString());
			}
		}
	}

	private Duel findDuel(String nick1, String nick2) {
		synchronized (duels) {
			for (Duel d : duels) {
				if ((d.firstPlayer.nick.equalsIgnoreCase(nick1) && d.secondPlayer.nick.equalsIgnoreCase(nick2)) || (d.secondPlayer.nick.equalsIgnoreCase(nick1) && d.firstPlayer.nick.equalsIgnoreCase(nick2))) {
					return d;
				}
			}
			return null;
		}
	}

	private List<Duel> findDuels(String nick) {
		synchronized (duels) {
			List<Duel> _duels = new ArrayList<>();
			for (Duel d : duels) {
				if (d.firstPlayer.nick.equalsIgnoreCase(nick)) {
					_duels.add(d);
				} else if (d.secondPlayer.nick.equalsIgnoreCase(nick)) {
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
			if (!CollectionUtils.isEmpty(duels)) {
				duels.removeIf(duel -> (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(quizSettings.getDuelInactivityTimeout())) > duel.lastActivity);
			}
		}
	}
	
	public List<Player> cleanScore() {
		log.debug("Resetting score...");
		List<Player> players = playerRepository.findByServerAndChannelIgnoreCase(bot.getServerHostname(), channel);
		log.info("Loaded {} players", players.size());
		int cnt = 0;
		try {
			for (Player p : players) {
				if (p.resetScore()) {
					cnt++;
				}
			}
			players = playerRepository.saveAll(players);
		} catch (Throwable t) {
			log.error("Error resetting score", t);
		} finally {
			log.debug("Reseted score of {} players.", cnt);
		}
		return players;
	}

	private void checkInactivity() {
		if (quizSettings.getChannelInactivityTimeout() > 0) {
			if (lastActivity < (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(quizSettings.getChannelInactivityTimeout()))) {
				log.debug("Stopping quiz due to inactivity. Last activity on channel was at: {}", TimeUtil.format(lastActivity));
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

		final PlayerDuelScore firstPlayer;
		final PlayerDuelScore secondPlayer;
		final int questions;
		final long initiatedAt;
		boolean confirmed;
		long startedAt;
		long lastActivity;

		Duel(String firstPlayerNick, String secondPlayerNick, int questions) {
			this.firstPlayer = new PlayerDuelScore(firstPlayerNick);
			this.secondPlayer = new PlayerDuelScore(secondPlayerNick);
			this.questions = questions;
			this.initiatedAt = System.currentTimeMillis();
			this.lastActivity = this.initiatedAt;
			this.confirmed = false;
		}

		void setLastActivity(long lastActivity) {
			this.lastActivity = lastActivity;
		}

		void confirm() {
			this.confirmed = true;
			this.startedAt = System.currentTimeMillis();
			this.lastActivity = this.startedAt;
		}

		PlayerDuelScore getDuelist(String nick) {
			if (firstPlayer.nick.equalsIgnoreCase(nick)) return firstPlayer;
			if (secondPlayer.nick.equalsIgnoreCase(nick)) return secondPlayer;
			return null;
		}

		@Override
		public String toString() {
			return "Dvoboj - " + (confirmed  ? "startan: " + TimeUtil.format(startedAt) : "iniciran: " + TimeUtil.format(initiatedAt)) + ", broj pitanja: " + questions + ", stanje: " + this.firstPlayer.nick + " (" + this.firstPlayer.score + ") :" + this.secondPlayer.nick + " (" + this.secondPlayer.score + ")";
		}

		boolean isFinished() {
			return firstPlayer.score >= questions || secondPlayer.score >= questions;
		}

		static class PlayerDuelScore {
			final String nick;
			int score;

			PlayerDuelScore(String nick) {
				this.nick = nick;
			}
		}
	}
}
