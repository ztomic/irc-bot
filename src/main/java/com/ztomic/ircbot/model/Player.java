package com.ztomic.ircbot.model;

import java.time.LocalDateTime;
import java.util.Comparator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.ztomic.ircbot.util.TimeUtil;
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

	@Column(name = "last_answered")
	private LocalDateTime lastAnswered;

	public void incrementDuelsWon() {
		this.duelsWon++;
	}

	public void incrementDuels() {
		this.duels++;
	}

	public void incrementScore(long points) {
		if (lastAnswered == null) {
			lastAnswered = LocalDateTime.now();
			weekScore = points;
			monthScore = points;
			score += points;
			return;
		}
		if (lastAnswered.isAfter(TimeUtil.getLocalDateTimeStartOfWeek())) {
			weekScore += points;
		} else {
			weekScore = points;
		}
		if (lastAnswered.isAfter(TimeUtil.getLocalDateTimeStartOfMonth())) {
			monthScore += points;
		} else {
			monthScore = points;
		}
		lastAnswered = LocalDateTime.now();
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
			if (weekScore != 0) {
				if (lastAnswered.isBefore(TimeUtil.getLocalDateTimeStartOfWeek())) {
					weekScore = 0;
					reset = true;
				}
			}
			if (monthScore != 0) {
				if (lastAnswered.isBefore(TimeUtil.getLocalDateTimeStartOfMonth())) {
					monthScore = 0;
					reset = true;
				}
			}
		}
		return reset;
	}

}