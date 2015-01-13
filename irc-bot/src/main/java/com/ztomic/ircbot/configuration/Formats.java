package com.ztomic.ircbot.configuration;


/**
 * Class which only contains constants for output, output is formatted in bot core before sending, also fields in this class can be updated via reflections during run.
 */
public class Formats {
//	public static String QUESTION_FORMAT = Colors.paintString(Colors.DARK_GREEN, "%s.") + Colors.paintString(Colors.GREEN, Colors.DARK_GRAY, "%s") + " - " + Colors.paintString(Colors.WHITE, Colors.BLACK, "%s");
//	public static String HINT1_FORMAT = Colors.paintString(Colors.DARK_GREEN, "1. Hint: %s") + Colors.paintString(Colors.BLUE, "Pitanje vrijedi:") + Colors.paintString(Colors.DARK_GREEN, "%s") + Colors.paintString(Colors.BLUE, "%s");
//	public static String HINT1_BONUS_FORMAT = Colors.paintString(Colors.DARK_GREEN, "1. Hint: %s") + Colors.paintString(Colors.BOLD, Colors.paintString(Colors.WHITE, Colors.RED, "BONUS!!!")) + Colors.paintString(Colors.BLUE, "Pitanje vrijedi:") + Colors.paintBoldString(4, "%s") + Colors.paintString(Colors.BLUE, "%s");
//	public static String HINT2_FORMAT = Colors.paintString(Colors.DARK_GREEN, "2. Hint: %s") + Colors.paintString(Colors.BLUE, "Preostaje:") + Colors.paintString(Colors.DARK_GREEN, "%s") + Colors.paintString(Colors.BLUE, "%s");
//	public static String HINT3_FORMAT = Colors.paintString(Colors.DARK_GREEN, "3. Hint: %s") + Colors.paintString(Colors.BLUE, "Preostaje:") + Colors.paintString(Colors.DARK_GREEN, "%s") + Colors.paintString(Colors.BLUE, "%s");
//	public static String TIMEUP_MESSAGE_FORMAT = Colors.paintString(Colors.BLUE, "Vrijeme je isteklo, prelazimo na slijedece pitanje.");
//	public static String LAST_ANSWER_FORMAT = Colors.paintString(Colors.BLUE, "Odgovor na prethodno pitanje bio je:") + Colors.paintBoldString(12, "%s");
//	public static String LAST_CHAR_FORMAT = Colors.paintString(Colors.DARK_GREEN, "Zadnje slovo:") + Colors.paintString(Colors.RED, "%s");
//	public static String VOWELS_FORMAT = Colors.paintString(Colors.DARK_GREEN, "Vokali:") + Colors.paintString(Colors.RED, "%s");
//	public static String FIRST_CHAR_FORMAT = Colors.paintString(Colors.DARK_GREEN, "Prvo slovo:") + Colors.paintString(Colors.RED, "%s");
//	public static String PERSONAL_STREAK_RECORD_COMMENT_FORMAT = Colors.paintString(Colors.BLUE, "Cestitamo, oborili ste osobni rekord u nizu odgovorenih pitanja i on sada iznosi:") + Colors.paintString(Colors.DARK_GREEN, "%s");
//	public static String CHANNEL_STREAK_RECORD_COMMENT_FORMAT = Colors.paintString(Colors.BLUE, "Cestitamo, %s, oborili ste rekord kanala u nizu odgovorenih pitanja i on sada iznosi:") + Colors.paintString(Colors.DARK_GREEN, "%s");
//	public static String AVAILABLE_COMMANDS_FORMAT = Colors.paintString(Colors.BLUE, "Available commands for your level (") + Colors.paintBoldString(Colors.RED, "%s") + Colors.paintString(Colors.BLUE, "):") + Colors.paintString(Colors.DARK_GREEN, "%s");
//	public static String LISTENER_AVAILABLE_COMMANDS_FORMAT = Colors.paintString(Colors.BLUE, "Available commands in module") + Colors.paintBoldString(Colors.DARK_GREEN, "%s") +  Colors.paintString(Colors.BLUE, "for your level (") + Colors.paintBoldString(Colors.RED, "%s") + Colors.paintString(Colors.BLUE, ") - prefix is: ") + Colors.paintString(Colors.RED, "%s") + Colors.paintString(Colors.BLUE, ":") + Colors.paintString(Colors.DARK_GREEN, "%s");
//	public static String SCORE_FORMAT = "Rezultati igraca: " + Colors.paintString(Colors.WHITE, Colors.BLACK, "%s") + "\nUkupan score:" + Colors.paintString(Colors.DARK_GREEN, "%s") + ", Najbrzi odgovor:" + Colors.paintString(Colors.DARK_GREEN, "%s") + ", Najdulji niz:" + Colors.paintString(Colors.DARK_GREEN, "%s") + ", Ovaj mjesec bodova:" + Colors.paintString(Colors.DARK_GREEN, "%s") + ", Ovaj tjedan bodova:" + Colors.paintString(Colors.DARK_GREEN, "%s");
//	public static String TEST_FORMAT = "{C}4%s{C}: {C}2,8Hello{O}, {B}how are {U}you{U}?{B}{C}12I am {R}good{O}.";
	public static String QUESTION_FORMAT = "{C}3 %s. {C}{C}9,14 %s {C} - {C}0,1 %s {C}";
	public static String HINT1_FORMAT = "{C}3 1. Hint: %s {C}{C}12 Pitanje vrijedi: {C}{C}3 %s {C}{C}12 %s {C}";
	public static String HINT1_BONUS_FORMAT = "{C}3 1. Hint: %s {C}{C}2 {C}0,4 BONUS!!! {C} {C}{C}12 Pitanje vrijedi: {C}{C}4{B}%s{B}{C}{C}12 %s {C}";
	public static String HINT1_CHALLENGE_FORMAT = "{C}3 1. Hint: %s {C}{C}2 {C}0,4 AJ SAD!!! {C} {C}{C}12 Pitanje vrijedi: {C}{C}4{B}%s{B}{C}{C}12 %s {C}";
	public static String HINT2_FORMAT = "{C}3 2. Hint: %s {C}{C}12 Preostaje: {C}{C}3 %s {C}{C}12 %s {C}";
	public static String HINT3_FORMAT = "{C}3 3. Hint: %s {C}{C}12 Preostaje: {C}{C}3 %s {C}{C}12 %s {C}";
	public static String GUESSED_FORMAT = "{C}3 Pogodjeno:{C}{C}4 %s";
	public static String TIMEUP_MESSAGE_FORMAT = "{C}12 Vrijeme je isteklo, prelazimo na slijedece pitanje. {C}";
	public static String LAST_ANSWER_FORMAT = "{C}12 Odgovor na prethodno pitanje bio je: {C}{C}12{B}%s{B}{C}";
	public static String LAST_CHAR_FORMAT = "{C}3 Zadnje slovo: {C}{C}4 %s {C}";
	public static String VOWELS_FORMAT = "{C}3 Vokali: {C}{C}4 %s {C}";
	public static String FIRST_CHAR_FORMAT = "{C}3 Prvo slovo: {C}{C}4 %s {C}";
	public static String PERSONAL_STREAK_RECORD_COMMENT_FORMAT = "{C}12 Cestitamo, oborili ste osobni rekord u nizu odgovorenih pitanja i on sada iznosi: {C}{C}3 %s {C}";
	public static String CHANNEL_STREAK_RECORD_COMMENT_FORMAT = "{C}12 Cestitamo, %s, oborili ste rekord kanala u nizu odgovorenih pitanja i on sada iznosi: {C}{C}3 %s {C}";
	public static String AVAILABLE_COMMANDS_FORMAT = "{C}12 Available commands for your level ( {C}{C}4{B}%s{B}{C}{C}12 ): {C}{C}3 %s {C}";
	public static String LISTENER_AVAILABLE_COMMANDS_FORMAT = "{C}12 Available commands in listener {C}{C}3{B}%s{B}{C}{C}12 for your level ( {C}{C}4{B}%s{B}{C}{C}12 ) - prefix is:  {C}{C}4 %s {C}{C}12 : {C}{C}3 %s {C}";
	public static String NO_AVAILABLE_COMMANDS_FORMAT = "No available commands found for you (your level is: {B}%s{B})";
	public static String SCORE_FORMAT = "Rezultati igraca: {C}0,1 %s {C}{NL}Ukupan score:{C}3 %s {C}, Najbrzi odgovor:{C}3 %s {C}, Najdulji niz:{C}3 %s {C}, Ovaj mjesec bodova:{C}3 %s {C}, Ovaj tjedan bodova:{C}3 %s {C}";
	public static String TEST_FORMAT = "{C}4%s{C}: {C}2,8Hello{O}, {B}how are {U}you{U}?{B}{C}12I am {R}good{O}.";
	
	public static String CHANGED_SETTING_FORMAT = "Value of {C}12%s{C} changed from{C}4 %s{C} to{C}3 %s{C}";
	public static String SETTING_VALUE_FORMAT = "Value of {C}12%s{C} is{C}3 %s{C}";
	
	public static String SEEN_NOT_FOUND_FORMAT = "Ne sjecam se nikoga s imenom: %s";
	public static String SEEN_ONLINE_FORMAT = "%s je na %s";
	public static String SEEN_SELF_FORMAT = "%s, pokusavate pronaci sebe?!";
	public static String SEEN_PART_FORMAT = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako napusta kanal {C}12%s{C} s porukom: {C}12%s{C}";
	public static String SEEN_NICK_FORMAT = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako mijenja nadimak u {C}9,14%s{C}";
	public static String SEEN_KICK_FORMAT = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} pri izbacivanju s kanala {C}12%s{C} od {C}3%s@%s{C} s porukom: {C}4%s{C}";
	public static String SEEN_QUIT_FORMAT = "{C}9,14%s{C} ({C}3%s@%s{C} - {C}3%s{C}) je zadnji put vidjen[a]{C}3 %s{C} kako napusta server s porukom: {C}4%s{C}";
	
	public static String JOIN_STATS_FORMAT = "%s: Ukupan score:{C}3 %s{C} (#{C}4 %s{C} ), mjesecni score:{C}3 %s{C} (#{C}4 %s{C} ), tjedni score:{C}3 %s{C} (#{C}4 %s{C} ), najbrzi odgovor{C}3 %s{C}sec (#{C}4 %s{C} ), najdulji niz:{C}3 %s{C} (#{C}4 %s{C} ), dvoboji:{C}3 %s{C} (#{C}4 %s{C} ), pobjede u dvobojima:{C}3 %s{C} (#{C}4 %s{C} )";
	
}
