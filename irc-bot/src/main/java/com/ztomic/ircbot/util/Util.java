package com.ztomic.ircbot.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.NumberUtils;

public final class Util {

	private Util() {
	}

	public static int parseInt(String s, int def) {
		try {
			return NumberUtils.parseNumber(s, Integer.class);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}

	public static long parseLong(String s, long def) {
		try {
			return NumberUtils.parseNumber(s, Long.class);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}

	public static boolean parseBool(String s) {
		return s != null && (s.toUpperCase().startsWith("Y") || s.toUpperCase().equals("TRUE"));
	}

	public static List<String> parseList(String s, String separator) {
		if (s == null) s = "";
		if (separator == null) separator = ";";
		return Arrays.asList(s.split(separator));
	}

}
