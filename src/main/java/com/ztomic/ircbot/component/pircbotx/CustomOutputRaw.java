package com.ztomic.ircbot.component.pircbotx;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ztomic.ircbot.util.Colors;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.PircBotX;
import org.pircbotx.Utils;
import org.pircbotx.output.OutputRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomOutputRaw extends OutputRaw {

	private static final Logger log = LoggerFactory.getLogger(CustomOutputRaw.class);
	
	public List<Long> timestamps = new ArrayList<>();
	private final double messageLimit;
	private final double messageLimitInterval;
	
	public CustomOutputRaw(PircBotX bot, double messageLimit, double messageLimitInterval) {
		super(bot);
		this.messageLimit = messageLimit;
		this.messageLimitInterval = messageLimitInterval;
	}
	
	public synchronized void checkDelay() throws InterruptedException {
    	double limit = messageLimit;
    	double interval = messageLimitInterval * 1000;
    	long now = System.currentTimeMillis();
		timestamps.removeIf(time -> time == null || time < (now - interval - 500));
    	Collections.sort(timestamps);
    	if (timestamps.size() >= limit) {
    		long delay = (timestamps.getFirst() + (long)interval + 501) - now;
    		Thread.sleep(delay); 
    	}
    	timestamps.add(System.currentTimeMillis());
    }

	public void rawLine(String line, String logline) {
		checkArgument(StringUtils.isNotBlank(line), "Cannot send empty line to server: '%s'", line);
		checkArgument(bot.isConnected(), "Not connected to server");

		limiter.acquire();
		
		writeLock.lock();


		try {
			//Block until we can send, taking into account a changing lastSentLine
			checkDelay();
			if (StringUtils.isNotBlank(logline)) {
				log.info(OUTPUT_MARKER, logline);
			} else {
				log.info(OUTPUT_MARKER, line);
			}
			Utils.sendRawLineToServer(bot, line);
		} catch (IOException e) {
			throw new RuntimeException("IO exception when sending line to server, is the network still up? " + exceptionDebug(), e);
		} catch (Exception e) {
			throw new RuntimeException("Could not send line to server. " + exceptionDebug(), e);
		} finally {
			writeLock.unlock();
		}
	}

	public void rawLineNow(String line, String logline) {
		checkNotNull(line, "Line cannot be null");
		checkArgument(bot.isConnected(), "Not connected to server");
		
		writeLock.lock();
		try {
			line = Colors.paintString(line);
			if (StringUtils.isNotBlank(logline)) {
				log.info(OUTPUT_MARKER, logline);
			} else {
				log.info(OUTPUT_MARKER, line);
			}
			Utils.sendRawLineToServer(bot, line);
		} catch (IOException e) {
			throw new RuntimeException("IO exception when sending line to server, is the network still up? " + exceptionDebug(), e);
		} catch (Exception e) {
			throw new RuntimeException("Could not send line to server. " + exceptionDebug(), e);
		} finally {
			writeLock.unlock();
		}
	}
	
	public void rawLineSplit(String prefix, String message, String suffix) {
		checkNotNull(prefix, "Prefix cannot be null");
		checkNotNull(message, "Message cannot be null");
		checkNotNull(suffix, "Suffix cannot be null");

		message = Colors.paintString(message);
		
		for (String m : message.split("\n")) {
			//Find if final line is going to be shorter than the max line length
			String finalMessage = prefix + m + suffix;
			int realMaxLineLength = bot.getConfiguration().getMaxLineLength() - 2;
			if (!bot.getConfiguration().isAutoSplitMessage() || finalMessage.length() < realMaxLineLength) {
				//Length is good (or auto split message is false), just go ahead and send it
				rawLine(finalMessage);
			} else {
				//Too long, split it up
				int maxMessageLength = realMaxLineLength - (prefix + suffix).length();
				for (String messagePart : splitOnWordsWithColors(m, maxMessageLength)) {
					String curMessagePart = prefix + messagePart + suffix;
					rawLine(curMessagePart);
				}
			}
		}
	}
	
	public static List<String> splitOnWordsWithColors(String text, int charLimit) {
		List<String> parts = Pattern.compile("(?=" + Colors.COLOR + "\\d)").splitAsStream(text).collect(Collectors.toList());
		if (parts.size() > 1) {
			List<String> lines = new LinkedList<>();
			while (!parts.isEmpty()) {
				StringBuilder line = new StringBuilder();
				while (line.length() < charLimit && !parts.isEmpty()) {
					if (line.length() + parts.getFirst().length() > charLimit) {
						break;
					}
					line.append(parts.removeFirst());
				}
				lines.add(line.toString());
			}
			if (!lines.isEmpty()) {
				List<String> output = new LinkedList<>();
				lines.forEach(line -> output.addAll(splitOnWords(line, charLimit)));
				return output;
			}
		}
		return splitOnWords(text, charLimit);
	}

	public static List<String> splitOnWords(String text, int charLimit) {
		List<String> output = new LinkedList<>();
		char[] chars = text.toCharArray();
		boolean endOfString = false;
		int start = 0;
		int end = start;
		while (start < chars.length - 1) {
			int charCount = 0;
			int lastSpace = 0;
			while (charCount < charLimit) {
				if (chars[charCount + start] == ' ') {
					lastSpace = charCount;
				}
				charCount++;
				if (charCount + start == text.length()) {
					endOfString = true;
					break;
				}
			}
			end = endOfString ? text.length() : (lastSpace > 0) ? lastSpace + start : charCount + start;
			output.add(text.substring(start, end));
			start = end + 1;
		}
		return output;
	}
	
}
