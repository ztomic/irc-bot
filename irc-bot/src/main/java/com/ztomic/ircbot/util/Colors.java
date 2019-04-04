/* 
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

 */

package com.ztomic.ircbot.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Colors class provides several static fields and methods that you may find
 * useful when writing an IRC Bot.
 * <p>
 * This class contains constants that are useful for formatting lines sent to
 * IRC servers. These constants allow you to apply various formatting to the
 * lines, such as colours, boldness, underlining and reverse text.
 * <p>
 * The class contains static methods to remove colours and formatting from lines
 * of IRC text.
 * <p>
 * Here are some examples of how to use the contants from within a class that
 * extends PircBot and imports org.jibble.pircbot.*;
 * 
 * <pre>
 * sendMessage("#cs", Colors.BOLD + "A bold hello!");
 *     <b>A bold hello!</b>
 * sendMessage("#cs", Colors.RED + "Red" + Colors.NORMAL + " text");
 *     <font color="red">Red</font> text
 * sendMessage("#cs", Colors.BOLD + Colors.RED + "Bold and red");
 *     <b><font color="red">Bold and red</font></b>
 * </pre>
 * 
 * Please note that some IRC channels may be configured to reject any messages
 * that use colours. Also note that older IRC clients may be unable to correctly
 * display lines that contain colours and other control characters.
 * <p>
 * Note that this class name has been spelt in the American style in order to
 * remain consistent with the rest of the Java API.
 * 
 * 
 * @since 0.9.12
 * @author Paul James Mutton, <a
 *         href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version 1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 */
public class Colors {

	/*
	 * public static enum Color { white, black, blue, green, lt_red, brown,
	 * purple, orange, yellow, lt_green, cyan, lt_cyan, lt_blue, pink, gray,
	 * lt_gray, none; }
	 */
	
	/**
	 * KVIrc-like color schemes for color of  nicknames
	 */
	public static String[] KVIRC_COLOR_SCHEMES = {
		"0,1", "0,2", "0,3", "0,4", "0,5", "0,6", "0,10", "0,12", "0,14",
		"1,0", "1,4", "1,7", "1,8", "1,9", "1,11", "1,15",
		"2,0", "2,4", "2,7", "2,8", "2,9", "2,11", "2,15",
		"3,8", "3,9", "3,0", "3,15",
		"4,0", "4,1", "4,8", "4,9", "4,11", "4,15",
		"5,0", "5,7", "5,8", "5,15",
		"6,0", "6,7", "6,8", "6,9", "6,10", "6,11", "6,15",
		"7,1", "7,2", "7,5", "7,6", "7,14",
		"8,1", "8,2", "8,3", "8,4", "8,5", "8,6", "8,7", "8,10", "8,12", "8,14",
		"9,1", "9,2", "9,3", "9,5", "9,6", "9,14",
		"10,1", "10,2",
		"11,1", "11,2", "11,3", "11,5", "11,6", "11,14",
		"12,0", "12,7", "12,8", "12,9", "12,10", "12,11", "12,15",
		"13,0", "13,1", "13,6", "13,8", "13,11", "13,15",
		"14,0", "14,8", "14,11", "14,15",
		"15,1", "15,2", "15,3", "15,6", "15,14"
		};
	
	/**
	 * IRC-like color codes for HTML
	 */
	public static String[] IRC_HTML_COLORS = {
		"#FFFFFF", "#000000", "#00008C", "#006400", "#E60000", "#960000", "#500050", "#FF5A00", "#FFFF00", "#00FF00", "#0096B4", "#AAAAFF", "#0F0FFF", "#C800C8", "#505050", "#AAAAAA"
	};
			


	/**
	 * Removes all previously applied color and formatting attributes.
	 */
	public static final char RESET = (char) 15;
	/** Bold text */
	public static final char BOLD = (char) 2;
	/** Colored text */
	public static final char COLOR = (char) 3;
	/** Underline text */
	public static final char UNDERLINE = (char) 31;
	/**
	 * Reversed text (may be rendered as italic text in some clients).
	 */
	public static final char REVERSE = (char) 22;

	/**
	 * Removes all previously applied color and formatting attributes.
	 */
	public static final char NORMAL = '\u000f';

	/**
	 * White coloured text.
	 */
	public static final int WHITE = 0;

	/**
	 * Black coloured text.
	 */
	public static final int BLACK = 1;

	/**
	 * Dark blue coloured text.
	 */
	public static final int DARK_BLUE = 2;

	/**
	 * Dark green coloured text.
	 */
	public static final int DARK_GREEN = 3;

	/**
	 * Red coloured text.
	 */
	public static final int RED = 4;

	/**
	 * Brown coloured text.
	 */
	public static final int BROWN = 5;

	/**
	 * Purple coloured text.
	 */
	public static final int PURPLE = 6;

	/**
	 * Olive coloured text.
	 */
	public static final int OLIVE = 7;

	/**
	 * Yellow coloured text.
	 */
	public static final int YELLOW = 8;

	/**
	 * Green coloured text.
	 */
	public static final int GREEN = 9;

	/**
	 * Teal coloured text.
	 */
	public static final int TEAL = 10;

	/**
	 * Cyan coloured text.
	 */
	public static final int CYAN = 11;

	/**
	 * Blue coloured text.
	 */
	public static final int BLUE = 12;

	/**
	 * Magenta coloured text.
	 */
	public static final int MAGENTA = 13;

	/**
	 * Dark gray coloured text.
	 */
	public static final int DARK_GRAY = 14;

	/**
	 * Light gray coloured text.
	 */
	public static final int LIGHT_GRAY = 15;

	/**
	 * This class should not be constructed.
	 */
	private Colors() {

	}

	/**
	 * Removes all colours from a line of IRC text.
	 * 
	 * @since PircBot 1.2.0
	 * 
	 * @param line
	 *            the input text.
	 * 
	 * @return the same text, but with all colours removed.
	 */
	public static String removeColors(String line) {
		int length = line.length();
		StringBuilder buffer = new StringBuilder();
		int i = 0;
		while (i < length) {
			char ch = line.charAt(i);
			if (ch == COLOR) {
				i++;
				// Skip "x" or "xy" (foreground color).
				if (i < length) {
					ch = line.charAt(i);
					if (Character.isDigit(ch)) {
						i++;
						if (i < length) {
							ch = line.charAt(i);
							if (Character.isDigit(ch)) {
								i++;
							}
						}
						// Now skip ",x" or ",xy" (background color).
						if (i < length) {
							ch = line.charAt(i);
							if (ch == ',') {
								i++;
								if (i < length) {
									ch = line.charAt(i);
									if (Character.isDigit(ch)) {
										i++;
										if (i < length) {
											ch = line.charAt(i);
											if (Character.isDigit(ch)) {
												i++;
											}
										}
									} else {
										// Keep the comma.
										i--;
									}
								} else {
									// Keep the comma.
									i--;
								}
							}
						}
					}
				}
			} else if (ch == RESET) {
				i++;
			} else {
				buffer.append(ch);
				i++;
			}
		}
		return buffer.toString();
	}

	/**
	 * Remove formatting from a line of IRC text.
	 * 
	 * @since PircBot 1.2.0
	 * 
	 * @param line
	 *            the input text.
	 * 
	 * @return the same text, but without any bold, underlining, reverse, etc.
	 */
	public static String removeFormatting(String line) {
		int length = line.length();
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char ch = line.charAt(i);
			if (ch != RESET && ch != BOLD && ch != UNDERLINE && ch != REVERSE) {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	/**
	 * Removes all formatting and colours from a line of IRC text.
	 * 
	 * @since PircBot 1.2.0
	 * 
	 * @param line
	 *            the input text.
	 * 
	 * @return the same text, but without formatting and colour characters.
	 * 
	 */
	public static String removeFormattingAndColors(String line) {
		return removeFormatting(removeColors(line));
	}

	/**
	 * Returns KVIrc-style colored nickname (nickname must be between <> in
	 * file, e.g. <nickname>)
	 * 
	 * @param nick
	 * @return
	 */
	public static String smartColoredNick(String nick) {
		String _nick = nick;
		List<Character> lIgnoreChars = Arrays.asList('@', '+');
		int n = 0;
		if (lIgnoreChars.contains(nick.charAt(0))) {
			_nick = nick.substring(1);
		}

		for (char c : _nick.toCharArray()) {
			n += (int) c;
		}

		int schema = n % KVIRC_COLOR_SCHEMES.length;
		String lFgBg = KVIRC_COLOR_SCHEMES[schema];
		return COLOR + lFgBg + " " + nick + " " + COLOR;

	}

	public static String paintBoldString(int color, Object text) {
		return COLOR + String.valueOf(color) + BOLD + text + BOLD + COLOR;
	}

	public static String paintString(int fg, int bg, Object text) {
		return COLOR + fg + "," + bg + " " + text + " " + COLOR;
	}

	public static String paintString(int fg, Object text) {
		return COLOR + String.valueOf(fg) + " " + text + " " + COLOR;
	}
	
	public static String paintString(String text) {
		if (text == null) {
			return text;
		}
		for (ColorTags c : ColorTags.values()) {
			text = text.replaceAll(Pattern.quote(c.regex), String.valueOf(c.replacement));
		}
		return text;
	}
	
	public enum ColorTags {
		C("{C}", COLOR), B("{B}", BOLD), U("{U}", UNDERLINE), O("{O}", RESET), R("{R}", REVERSE), NL("{NL}", '\n');
		
		public String regex;
		public char replacement;
		
		ColorTags(String regex, char replacement) {
			this.regex = regex;
			this.replacement = replacement;
		}
	}

}
