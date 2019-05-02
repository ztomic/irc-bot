package com.ztomic.ircbot.listener.quiz;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

@Data
public class QuizSettings {

	public String commandPrefix = "-";

	public int sameCmdDelaySec = 15;

	public int hintDelaySec = 15;
	public int questionDelaySec = 2;
	public char hintChar = '°';

	public double firstCharPoints = 0.25;
	public double lastCharPoints = 0.25;
	public double vowelPoints = 0.25;
	public double numberPoints = 1;
	public double letterPoints = 0.5;

	public int bonusRandom = 20;
	public int bonusMatch = 5;

	public int bonusOnLength = 35;
	public int bonusOnLengthFactor = 10;

	public boolean showGuessed = true;
	public int showGuessedPhase = 1;
	/** in minutes */
	public int duelInactivityTimeout = 60;
	public boolean sendGreet = true;
	public List<String> ignoredNicks = Arrays.asList("NickServ", "ChanServ", "MemoServ");

	public int channelInactivityTimeout = 0;
	
}
