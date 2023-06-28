package com.ztomic.ircbot.configuration;

import lombok.Data;

@Data
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
	private String testFormat = "{C}4%s{C}: {C}2,8Hello{O}, {B}how are {U}you{U}?{B}{C}12 I am {R}good{O}.";

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
	
}
