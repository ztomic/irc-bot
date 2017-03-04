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

import lombok.Data;

@Entity
@Table(name = "players")
@Data
public class Player {

	public static final Comparator<Player> CMP_BY_STREAK_ASC = Comparator.comparing(Player::getRowRecord).reversed();
	public static final Comparator<Player> CMP_BY_SCORE = Comparator.comparing(Player::getScore).reversed();
	public static final Comparator<Player> CMP_BY_SPEED_ASC = Comparator.comparing(Player::getFastestTime).thenComparing(CMP_BY_SCORE);
	public static final Comparator<Player> CMP_BY_WEEK_SCORE = Comparator.comparing(Player::getWeekScore).reversed();
	public static final Comparator<Player> CMP_BY_MONTH_SCORE = Comparator.comparing(Player::getMonthScore).reversed();
	public static final Comparator<Player> CMP_BY_DUELS_WON = Comparator.comparing(Player::getDuelsWon).reversed();
	public static final Comparator<Player> CMP_BY_DUELS = Comparator.comparing(Player::getDuels).reversed().thenComparing(CMP_BY_DUELS_WON);
	
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

	public void incrementDuelsWon() {
		this.duelsWon++;
	}

	public void incrementDuels() {
		this.duels++;
	}

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

}