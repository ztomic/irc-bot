package com.ztomic.ircbot.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class TimeUtil {

	public static LocalDateTime getLocalDateTimeStartOfWeek() {
		return LocalDateTime.now()
				.with(ChronoField.DAY_OF_WEEK, 1)
				.truncatedTo(ChronoUnit.DAYS);
	}

	public static LocalDateTime getLocalDateTimeStartOfMonth() {
		return LocalDateTime.now()
				.withDayOfMonth(1)
				.truncatedTo(ChronoUnit.DAYS);
	}

	public static LocalDateTime getLocalDateTime(long epochMilli) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
	}

	public static String format(long epochMilli) {
		return format(getLocalDateTime(epochMilli));
	}

	public static String format(Instant instant) {
		return format(instant.toEpochMilli());
	}

	public static String format(LocalDateTime temporal) {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(temporal);
	}

	public static String format(LocalDate temporal) {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(temporal);
	}
}
