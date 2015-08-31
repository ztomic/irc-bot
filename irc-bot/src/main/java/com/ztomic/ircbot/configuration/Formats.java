package com.ztomic.ircbot.configuration;

public class Formats {

	private String questionFormat = "{C}3 %s. {C}{C}9,14 %s {C} - {C}0,1 %s {C}";
	private String hint1Format = "{C}3 1. Hint: %s {C}{C}12 Pitanje vrijedi: {C}{C}3 %s {C}{C}12 %s {C}";
	private String hint1BonusFormat = "{C}3 1. Hint: %s {C}{C}2 {C}0,4 BONUS!!! {C} {C}{C}12 Pitanje vrijedi: {C}{C}4{B}%s{B}{C}{C}12 %s {C}";
	private String hint1ChallengeFormat = "{C}3 1. Hint: %s {C}{C}2 {C}0,4 AJ SAD!!! {C} {C}{C}12 Pitanje vrijedi: {C}{C}4{B}%s{B}{C}{C}12 %s {C}";
	private String hint2Format = "{C}3 2. Hint: %s {C}{C}12 Preostaje: {C}{C}3 %s {C}{C}12 %s {C}";
	private String hint3Format = "{C}3 3. Hint: %s {C}{C}12 Preostaje: {C}{C}3 %s {C}{C}12 %s {C}";
	private String guessedFormat = "{C}3 Pogodjeno:{C}{C}4 %s";
	private String timeupMessageFormat = "{C}12 Vrijeme je isteklo, prelazimo na slijedece pitanje. {C}";
	private String lastAnswerFormat = "{C}12 Odgovor na prethodno pitanje bio je: {C}{C}12{B}%s{B}{C}";
	private String lastCharFormat = "{C}3 Zadnje slovo: {C}{C}4 %s {C}";
	private String vowelsFormat = "{C}3 Vokali: {C}{C}4 %s {C}";
	private String firstCharFormat = "{C}3 Prvo slovo: {C}{C}4 %s {C}";

	private String personalStreakRecordCommentFormat = "{C}12 Cestitamo, oborili ste osobni rekord u nizu odgovorenih pitanja i on sada iznosi: {C}{C}3 %s {C}";
	private String channelStreakRecordCommentFormat = "{C}12 Cestitamo, %s, oborili ste rekord kanala u nizu odgovorenih pitanja i on sada iznosi: {C}{C}3 %s {C}";
	private String availableCommandsFormat = "{C}12 Available commands for your level ( {C}{C}4{B}%s{B}{C}{C}12 ): {C}{C}3 %s {C}";
	private String listenerAvailableCommandsFormat = "{C}12 Available commands in listener {C}{C}3{B}%s{B}{C}{C}12 for your level ( {C}{C}4{B}%s{B}{C}{C}12 ) - prefix is:  {C}{C}4 %s {C}{C}12 : {C}{C}3 %s {C}";
	private String noAvailableCommandsFormat = "No available commands found for you (your level is: {B}%s{B})";
	private String scoreFormat = "Rezultati igraca: {C}0,1 %s {C}{NL}Ukupan score:{C}3 %s {C}, Najbrzi odgovor:{C}3 %s {C}, Najdulji niz:{C}3 %s {C}, Ovaj mjesec bodova:{C}3 %s {C}, Ovaj tjedan bodova:{C}3 %s {C}";
	private String testFormat = "{C}4%s{C}: {C}2,8Hello{O}, {B}how are {U}you{U}?{B}{C}12I am {R}good{O}.";

	private String changedSettingFormat = "Value of {C}12%s{C} changed from{C}4 %s{C} to{C}3 %s{C}";
	private String settingValueFormat = "Value of {C}12%s{C} is{C}3 %s{C}";
	private String seenNotFoundFormat = "Ne sjecam se nikoga s imenom: %s";
	private String seenOnlineFormat = "%s je na %s";
	private String seenSelfFormat = "%s, pokusavate pronaci sebe?!";
	private String seenPartFormat = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako napusta kanal {C}12%s{C} s porukom: {C}12%s{C}";
	private String seenNickFormat = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako mijenja nadimak u {C}9,14%s{C}";
	private String seenKickFormat = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} pri izbacivanju s kanala {C}12%s{C} od {C}3%s@%s{C} s porukom: {C}4%s{C}";
	private String seenQuitFormat = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako napusta server s porukom: {C}4%s{C}";

	private String joinStatsFormat = "%s: Ukupan score:{C}3 %s{C} (#{C}4 %s{C} ), mjesecni score:{C}3 %s{C} (#{C}4 %s{C} ), tjedni score:{C}3 %s{C} (#{C}4 %s{C} ), najbrzi odgovor{C}3 %s{C}sec (#{C}4 %s{C} ), najdulji niz:{C}3 %s{C} (#{C}4 %s{C} ), dvoboji:{C}3 %s{C} (#{C}4 %s{C} ), pobjede u dvobojima:{C}3 %s{C} (#{C}4 %s{C} )";

	public String getQuestionFormat() {
		return questionFormat;
	}

	public void setQuestionFormat(String questionFormat) {
		this.questionFormat = questionFormat;
	}

	public String getHint1Format() {
		return hint1Format;
	}

	public void setHint1Format(String hint1Format) {
		this.hint1Format = hint1Format;
	}

	public String getHint1BonusFormat() {
		return hint1BonusFormat;
	}

	public void setHint1BonusFormat(String hint1BonusFormat) {
		this.hint1BonusFormat = hint1BonusFormat;
	}

	public String getHint1ChallengeFormat() {
		return hint1ChallengeFormat;
	}

	public void setHint1ChallengeFormat(String hint1ChallengeFormat) {
		this.hint1ChallengeFormat = hint1ChallengeFormat;
	}

	public String getHint2Format() {
		return hint2Format;
	}

	public void setHint2Format(String hint2Format) {
		this.hint2Format = hint2Format;
	}

	public String getHint3Format() {
		return hint3Format;
	}

	public void setHint3Format(String hint3Format) {
		this.hint3Format = hint3Format;
	}

	public String getGuessedFormat() {
		return guessedFormat;
	}

	public void setGuessedFormat(String guessedFormat) {
		this.guessedFormat = guessedFormat;
	}

	public String getTimeupMessageFormat() {
		return timeupMessageFormat;
	}

	public void setTimeupMessageFormat(String timeupMessageFormat) {
		this.timeupMessageFormat = timeupMessageFormat;
	}

	public String getLastAnswerFormat() {
		return lastAnswerFormat;
	}

	public void setLastAnswerFormat(String lastAnswerFormat) {
		this.lastAnswerFormat = lastAnswerFormat;
	}

	public String getLastCharFormat() {
		return lastCharFormat;
	}

	public void setLastCharFormat(String lastCharFormat) {
		this.lastCharFormat = lastCharFormat;
	}

	public String getVowelsFormat() {
		return vowelsFormat;
	}

	public void setVowelsFormat(String vowelsFormat) {
		this.vowelsFormat = vowelsFormat;
	}

	public String getFirstCharFormat() {
		return firstCharFormat;
	}

	public void setFirstCharFormat(String firstCharFormat) {
		this.firstCharFormat = firstCharFormat;
	}

	public String getPersonalStreakRecordCommentFormat() {
		return personalStreakRecordCommentFormat;
	}

	public void setPersonalStreakRecordCommentFormat(String personalStreakRecordCommentFormat) {
		this.personalStreakRecordCommentFormat = personalStreakRecordCommentFormat;
	}

	public String getChannelStreakRecordCommentFormat() {
		return channelStreakRecordCommentFormat;
	}

	public void setChannelStreakRecordCommentFormat(String channelStreakRecordCommentFormat) {
		this.channelStreakRecordCommentFormat = channelStreakRecordCommentFormat;
	}

	public String getAvailableCommandsFormat() {
		return availableCommandsFormat;
	}

	public void setAvailableCommandsFormat(String availableCommandsFormat) {
		this.availableCommandsFormat = availableCommandsFormat;
	}

	public String getListenerAvailableCommandsFormat() {
		return listenerAvailableCommandsFormat;
	}

	public void setListenerAvailableCommandsFormat(String listenerAvailableCommandsFormat) {
		this.listenerAvailableCommandsFormat = listenerAvailableCommandsFormat;
	}

	public String getNoAvailableCommandsFormat() {
		return noAvailableCommandsFormat;
	}

	public void setNoAvailableCommandsFormat(String noAvailableCommandsFormat) {
		this.noAvailableCommandsFormat = noAvailableCommandsFormat;
	}

	public String getScoreFormat() {
		return scoreFormat;
	}

	public void setScoreFormat(String scoreFormat) {
		this.scoreFormat = scoreFormat;
	}

	public String getTestFormat() {
		return testFormat;
	}

	public void setTestFormat(String testFormat) {
		this.testFormat = testFormat;
	}

	public String getChangedSettingFormat() {
		return changedSettingFormat;
	}

	public void setChangedSettingFormat(String changedSettingFormat) {
		this.changedSettingFormat = changedSettingFormat;
	}

	public String getSettingValueFormat() {
		return settingValueFormat;
	}

	public void setSettingValueFormat(String settingValueFormat) {
		this.settingValueFormat = settingValueFormat;
	}

	public String getSeenNotFoundFormat() {
		return seenNotFoundFormat;
	}

	public void setSeenNotFoundFormat(String seenNotFoundFormat) {
		this.seenNotFoundFormat = seenNotFoundFormat;
	}

	public String getSeenOnlineFormat() {
		return seenOnlineFormat;
	}

	public void setSeenOnlineFormat(String seenOnlineFormat) {
		this.seenOnlineFormat = seenOnlineFormat;
	}

	public String getSeenSelfFormat() {
		return seenSelfFormat;
	}

	public void setSeenSelfFormat(String seenSelfFormat) {
		this.seenSelfFormat = seenSelfFormat;
	}

	public String getSeenPartFormat() {
		return seenPartFormat;
	}

	public void setSeenPartFormat(String seenPartFormat) {
		this.seenPartFormat = seenPartFormat;
	}

	public String getSeenNickFormat() {
		return seenNickFormat;
	}

	public void setSeenNickFormat(String seenNickFormat) {
		this.seenNickFormat = seenNickFormat;
	}

	public String getSeenKickFormat() {
		return seenKickFormat;
	}

	public void setSeenKickFormat(String seenKickFormat) {
		this.seenKickFormat = seenKickFormat;
	}

	public String getSeenQuitFormat() {
		return seenQuitFormat;
	}

	public void setSeenQuitFormat(String seenQuitFormat) {
		this.seenQuitFormat = seenQuitFormat;
	}

	public String getJoinStatsFormat() {
		return joinStatsFormat;
	}

	public void setJoinStatsFormat(String joinStatsFormat) {
		this.joinStatsFormat = joinStatsFormat;
	}

	@Override
	public String toString() {
		return "Formats [questionFormat=" + questionFormat + ", hint1Format=" + hint1Format + ", hint1BonusFormat=" + hint1BonusFormat + ", hint1ChallengeFormat=" + hint1ChallengeFormat + ", hint2Format=" + hint2Format + ", hint3Format=" + hint3Format + ", guessedFormat=" + guessedFormat + ", timeupMessageFormat=" + timeupMessageFormat + ", lastAnswerFormat=" + lastAnswerFormat + ", lastCharFormat=" + lastCharFormat + ", vowelsFormat=" + vowelsFormat + ", firstCharFormat=" + firstCharFormat + ", personalStreakRecordCommentFormat=" + personalStreakRecordCommentFormat + ", channelStreakRecordCommentFormat=" + channelStreakRecordCommentFormat + ", availableCommandsFormat=" + availableCommandsFormat + ", listenerAvailableCommandsFormat=" + listenerAvailableCommandsFormat + ", noAvailableCommandsFormat=" + noAvailableCommandsFormat + ", scoreFormat=" + scoreFormat + ", testFormat=" + testFormat + ", changedSettingFormat=" + changedSettingFormat + ", settingValueFormat=" + settingValueFormat + ", seenNotFoundFormat=" + seenNotFoundFormat + ", seenOnlineFormat=" + seenOnlineFormat + ", seenSelfFormat=" + seenSelfFormat + ", seenPartFormat=" + seenPartFormat + ", seenNickFormat=" + seenNickFormat + ", seenKickFormat=" + seenKickFormat + ", seenQuitFormat=" + seenQuitFormat + ", joinStatsFormat=" + joinStatsFormat + "]";
	}
	
	
}
