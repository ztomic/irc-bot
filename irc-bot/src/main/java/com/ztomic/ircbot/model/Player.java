package com.ztomic.ircbot.model;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "players")
public class Player {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "nick")
	private String nick;

	@Column(name = "server")
	private String server;

	@Column(name = "channel")
	private String channel;

	@Column(name = "score")
	private long score;

	@Column(name = "month_score")
	private long monthScore;

	@Column(name = "week_score")
	private long weekScore;

	@Column(name = "fastest_time")
	private long fastestTime;

	@Column(name = "row_record")
	private int rowRecord;

	@Column(name = "duels")
	private int duels;

	@Column(name = "duels_won")
	private int duelsWon;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_answered")
	private Date lastAnswered;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getMonthScore() {
		return monthScore;
	}

	public void setMonthScore(long monthScore) {
		this.monthScore = monthScore;
	}

	public long getWeekScore() {
		return weekScore;
	}

	public void setWeekScore(long weekScore) {
		this.weekScore = weekScore;
	}

	public long getFastestTime() {
		return fastestTime;
	}

	public void setFastestTime(long fastestTime) {
		this.fastestTime = fastestTime;
	}

	public int getRowRecord() {
		return rowRecord;
	}

	public void setRowRecord(int rowRecord) {
		this.rowRecord = rowRecord;
	}

	public int getDuels() {
		return duels;
	}

	public void setDuels(int duels) {
		this.duels = duels;
	}

	public int getDuelsWon() {
		return duelsWon;
	}

	public void setDuelsWon(int duelsWon) {
		this.duelsWon = duelsWon;
	}

	public Date getLastAnswered() {
		return lastAnswered;
	}

	public void setLastAnswered(Date lastAnswered) {
		this.lastAnswered = lastAnswered;
	}

	public void incrementDuelsWon() {
		this.duelsWon++;
	}

	public void incrementDuels() {
		this.duels++;
	}

	public static final Comparator<Player> CMP_BY_SPEED_ASC = (o1, o2) -> {
		if (o1.fastestTime == 0 && o2.fastestTime == 0)
			return 0;
		if (o1.fastestTime != 0 && o2.fastestTime == 0)
			return -1;
		if (o1.fastestTime == 0)
			return 1;
		return Long.valueOf(o1.fastestTime).compareTo(o2.fastestTime);
	};

	public static final Comparator<Player> CMP_BY_STREAK_ASC = (o1, o2) -> Integer.valueOf(o2.rowRecord).compareTo(o1.rowRecord);

	public static final Comparator<Player> CMP_BY_SCORE = (o1, o2) -> Long.valueOf(o2.score).compareTo(o1.score);

	public static final Comparator<Player> CMP_BY_WEEK_SCORE = (o1, o2) -> Long.valueOf(o2.weekScore).compareTo(o1.weekScore);

	public static final Comparator<Player> CMP_BY_MONTH_SCORE = (o1, o2) -> Long.valueOf(o2.monthScore).compareTo(o1.monthScore);

	public static final Comparator<Player> CMP_BY_DUELS = (o1, o2) -> {
		if (o1.duels == o2.duels)
			return Long.valueOf(o2.duelsWon).compareTo((long) o1.duelsWon);
		return Long.valueOf(o2.duels).compareTo((long) o1.duels);
	};

	public static final Comparator<Player> CMP_BY_DUELS_WON = (o1, o2) -> Long.valueOf(o2.duelsWon).compareTo((long) o1.duelsWon);

	public void incrementScore(long points) {
		if (lastAnswered == null) {
			lastAnswered = new Date();
			weekScore = points;
			monthScore = points;
			score += points;
			return;
		}
		Calendar weekStart = Calendar.getInstance();
		weekStart.setFirstDayOfWeek(Calendar.MONDAY);
		weekStart.set(Calendar.HOUR, 0);
		weekStart.set(Calendar.MINUTE, 0);
		weekStart.set(Calendar.SECOND, 0);
		weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		if (lastAnswered.getTime() >= weekStart.getTimeInMillis()) {
			weekScore += points;
		} else {
			weekScore = points;
		}
		weekStart.set(Calendar.DATE, 1);
		if (lastAnswered.getTime() >= weekStart.getTimeInMillis()) {
			monthScore += points;
		} else {
			monthScore = points;
		}
		lastAnswered = new Date();
		score += points;
	}

	public boolean resetScore() {
		boolean reset = false;
		if (weekScore != 0 || monthScore != 0) {
			if (lastAnswered == null) {
				weekScore = 0;
				monthScore = 0;
				return true;
			}
			Calendar weekStart = Calendar.getInstance();
			weekStart.setFirstDayOfWeek(Calendar.MONDAY);
			weekStart.set(Calendar.HOUR, 0);
			weekStart.set(Calendar.MINUTE, 0);
			weekStart.set(Calendar.SECOND, 0);
			weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			if (weekScore != 0) {
				if (lastAnswered.getTime() <= weekStart.getTimeInMillis()) {
					weekScore = 0;
					reset = true;
				}
			}
			weekStart.set(Calendar.DATE, 1);
			if (monthScore != 0) {
				if (lastAnswered.getTime() <= weekStart.getTimeInMillis()) {
					monthScore = 0;
					reset = true;
				}
			}
		}
		return reset;
	}

	@Override
	public String toString() {
		return String.format("Player [id=%s, nick=%s, channel=%s, server=%s, score=%s, fastestTime=%s, rowRecord=%s, monthScore=%s, weekScore=%s, lastAnswered=%s]", id, nick, channel, server, score, fastestTime, rowRecord, monthScore, weekScore, lastAnswered);
	}

}