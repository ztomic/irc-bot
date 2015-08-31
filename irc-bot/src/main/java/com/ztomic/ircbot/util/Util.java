package com.ztomic.ircbot.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.util.NumberUtils;

public final class Util {
	
	private Util() {}

	public static int parseInt(String s, int def) {
		try {
			return NumberUtils.parseNumber(s, Integer.class);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}

	public static boolean parseBool(String s) {
		if (s == null)
			return false;
		return s.toUpperCase().startsWith("Y") || s.toUpperCase().equals("TRUE");
	}

	public static String formatDate(Date d, String pattern) {
		if (d == null)
			return "";
		if (pattern == null)
			pattern = "yyyy-MM-dd HH:mm:ss";
		return new SimpleDateFormat(pattern).format(d);
	}
	
	public static List<String> parseList(String s, String separator) {
		if (s == null) s = "";
		if (separator == null) separator = ";";
		return Arrays.asList(s.split(separator));
	}

}
