package com.ztomic.ircbot.util;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
	
	private Util() {}
	
	public static final String DEFAULT_ENCODING = "UTF-8"; // System.getProperty("file.encoding");
	
	/**
	 * Returns external IP address by sending HTTP request to http://whatismyip.com/automation/n09230945.asp
	 * @return external IP address
	 */
	public static String getExternalIPAddress() {
		String url = null;
		try {
			java.net.URL URL = new java.net.URL("http://whatismyip.com/automation/n09230945.asp");
			java.net.HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			java.io.InputStream InStream = conn.getInputStream();
			java.io.InputStreamReader Isr = new java.io.InputStreamReader(InStream);
			java.io.BufferedReader br = new java.io.BufferedReader(Isr);
			url = br.readLine();
		} catch (Exception e) {
			//
		}
		return url;
	}
	
	/**
	 * Finds external IP address by sending http request to <b>host</b> with expected response in pattern <b>regexPattern</b>
	 * @param host remote host
	 * @param regexPattern pattern of response, ip address pattern should be in group, e.g. <b>.+&lt;body&gt;Current IP Address: (\\d+.\\d+.\\d+.\\d+)&lt;/body&gt;.+</b>
	 * @return IP address if matched with regexPattern
	 */
	public static String getExternalIP(String host, String regexPattern) {
		String resp = null;
		if (regexPattern == null) regexPattern = ".+<body>Current IP Address: (\\d+.\\d+.\\d+.\\d+)</body>.+";
		if (host == null) host = "http://checkip.dyndns.org:8245/";
		Pattern pattern = Pattern.compile(regexPattern);
		try {
			java.net.URL URL = new java.net.URL(host);
			java.net.HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			java.io.InputStream InStream = conn.getInputStream();
			java.io.InputStreamReader Isr = new java.io.InputStreamReader(InStream);
			java.io.BufferedReader br = new java.io.BufferedReader(Isr);
			resp = br.readLine();
			if (resp != null) {
				Matcher matcher = pattern.matcher(resp);
				if (matcher.matches()) {
					return matcher.group(1);
				}
				
			}
		} catch (Exception e) {
			//
		}
		return resp;
	}
	
	/**
	 * Returns external IP address by sending HTTP request to http://checkip.dyndns.org:8425
	 * @return external IP address by sending HTTP request to http://checkip.dyndns.org:8425
	 */
	public static String getExternalIP() {
		return getExternalIP("http://checkip.dyndns.org:8245/", ".+<body>Current IP Address: (\\d+.\\d+.\\d+.\\d+)</body>.+");
	}

	public static int parseInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}

	public static long parseLong(String s, long def) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}
	
	public static float parseFloat(String s, float def) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}
	
	public static short parseShort(String s, short def) {
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	public static double parseDouble(String s, double def) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return def;
		}
		
	}

	public static int parseIntHex(String s, int def) {
		try {
			return Integer.parseInt(s, 16);
		} catch (NumberFormatException nfe) {
			return def;
		}
	}

	public static boolean parseBool(String s) {
		if (s == null)
			return false;
		return s.toUpperCase().startsWith("Y") || s.toUpperCase().equals("TRUE");
	}

	public static int checkRange(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}
	
	public static String formatDate(Date d, String pattern) {
		if (d == null)
			return "";
		if (pattern == null)
			pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(d);
	}

	/**
	 * Creates the concatenated string from the object set and separate items with specified delimiter.
	 * 
	 * @param set
	 *            the object set.
	 * @param delimiter
	 *            the separation delimiter.
	 * @return the concatenated string from the object set.
	 */
	public static String delimitSet(Set<String> set, String delimiter) {
		return formatCollection(set, delimiter);
	}

	/**
	 * Creates the concatenated string from the object list and separate items with specified delimiter.
	 * 
	 * @param list
	 *            the object list.
	 * @param delimiter
	 *            the separation delimiter.
	 * @return the concatenated string from the object list.
	 */
	public static String delimitList(List<?> list, String delimiter) {
		return formatCollection(list, delimiter);
	}
	
	public static String formatList(List<?> l, String separator) {
		return formatCollection(l, separator);
	}
	
	public static String formatCollection(Collection<?> col, String separator) {
		StringBuilder sb = new StringBuilder();
		if (col == null)
			return "";
		for (Iterator<?> i = col.iterator(); i.hasNext();) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}
	
	public static List<String> parseList(String s, String separator) {
		if (s == null) s = "";
		if (separator == null) separator = ";";
		
		return Arrays.asList(s.split(separator));
	}

	public static String[] splitString(String string, int len) {
		if (string.length() <= len) {
			return new String[] { string };
		}

		int beginIndex = 0;
		int endIndex = 0;

		int size = string.length() / len;
		if ((string.length() % len) != 0) {
			size++;
		}

		String[] parts = new String[size];

		for (int i = 0; i < size; i++) {
			endIndex = beginIndex + len;
			if (endIndex > string.length() - 1) {
				endIndex = string.length();
			}

			parts[i] = string.substring(beginIndex, endIndex);

			beginIndex = beginIndex + len;
		}

		return parts;
	}
	
	/**
	 * Converts local time to UTC time properly
	 * @param date the Date object
	 * @return date converted to <b>UTC</b>
	 */
	public static Date getUTCDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime() - cal.getTimeZone().getOffset(date.getTime()));
		return cal.getTime();
	}
	
	/**
	 * Splits string on parts of <b>maxLen</b> size.
	 * 
	 * @param str
	 * @param maxLen
	 * @return java.lang.String Array
	 */
	public static String[] splitOnLength(String str, int maxLen) {
		int origLen = str.getBytes().length;
		int splitNum = origLen / maxLen;
		if (origLen % maxLen > 0)
			splitNum += 1;

		String[] splits = new String[splitNum];

		for (int i = 0; i < splitNum; i++) {
			int startPos = i * maxLen;
			int endPos = startPos + maxLen;
			if (endPos > origLen)
				endPos = origLen;
			String substr = str.substring(startPos, endPos);
			splits[i] = substr;
		}

		return splits;
	}

	/**
	 * Concatenates array of objects to string value, separate and wrap with
	 * specified delimiter and wrapper.
	 * 
	 * @param arr
	 *            the array of objects.
	 * @param delimiter
	 *            the separation delimiter.
	 * @param wrap
	 *            the wrap value.
	 * @return the string concatenation of object array.
	 */
	public static String delimitArray(Object[] arr, String delimiter, String wrap) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			sb.append(wrap).append(arr[i]).append(wrap);
			if (i + 1 < arr.length) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	/**
	 * Creates the string from the object list and separate items with specified
	 * delimiter.
	 * 
	 * @param list
	 *            the object list.
	 * @param delimiter
	 *            the separation delimiter.
	 * @return the string from the object list.
	 */
	public static String delimitList(List<String> list, String delimiter, String wrap) {
		StringBuilder sb = new StringBuilder();
		if (list == null)
			return "";
		for (Iterator<String> i = list.iterator(); i.hasNext();) {
			sb.append(wrap);
			sb.append(i.next());
			sb.append(wrap);
			if (i.hasNext()) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
	
	public static String delimitCollection(Collection<String> collection, String delimiter, String wrap) {
		StringBuilder sb = new StringBuilder();
		if (collection == null)
			return "";
		for (Iterator<String> i = collection.iterator(); i.hasNext();) {
			sb.append(wrap);
			sb.append(i.next());
			sb.append(wrap);
			if (i.hasNext()) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
	
}
