package com.ztomic.ircbot.component.pircbotx;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pircbotx.PircBotX;
import org.pircbotx.Utils;
import org.pircbotx.output.OutputRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ztomic.ircbot.util.Colors;

public class CustomOutputRaw extends OutputRaw {

	private static final Logger log = LoggerFactory.getLogger(CustomOutputRaw.class);
	
	public List<Long> timestamps = new ArrayList<>();
	private double messageLimit;
	private double messageLimitInterval;
	
	public CustomOutputRaw(PircBotX bot, double messageLimit, double messageLimitInterval) {
		super(bot);
		this.messageLimit = messageLimit;
		this.messageLimitInterval = messageLimitInterval;
	}
	
	public synchronized void checkDelay() throws InterruptedException {
    	double limit = messageLimit;
    	double interval = messageLimitInterval * 1000;
    	long now = System.currentTimeMillis();
    	for (Iterator<Long> iterator = timestamps.iterator(); iterator.hasNext();) {
    		Long time = iterator.next();
			if (time == null || time < (now-interval-500)) {
                iterator.remove();
            }
		}
    	Collections.sort(timestamps);
    	if (timestamps.size() >= limit) {
    		long delay = (timestamps.get(0) + (long)interval + 501) - now;
    		Thread.sleep(delay); 
    	}
    	timestamps.add(System.currentTimeMillis());
    }
	
	public void rawLine(String line) {
		checkNotNull(line, "Line cannot be null");
		if (!bot.isConnected())
			throw new RuntimeException("Not connected to server");
		writeLock.lock();
		try {
			//Block until we can send, taking into account a changing lastSentLine
			checkDelay();
			log.info(OUTPUT_MARKER, line);
			Utils.sendRawLineToServer(bot, line);
			lastSentLine = System.nanoTime();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't pause thread for message delay", e);
		} finally {
			writeLock.unlock();
		}
	}
	
	public void rawLineNow(String line, boolean resetDelay) {
		checkNotNull(line, "Line cannot be null");
		if (!bot.isConnected())
			throw new RuntimeException("Not connected to server");
		writeLock.lock();
		try {
			line = Colors.paintString(line);
			log.info(OUTPUT_MARKER, line);
			Utils.sendRawLineToServer(bot, line);
			lastSentLine = System.nanoTime();
			if (resetDelay)
				//Reset the 
				writeNowCondition.signalAll();
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
				for (String messagePart : splitOnWords(m, maxMessageLength)) {
					String curMessagePart = prefix + messagePart + suffix;
					rawLine(curMessagePart);
				}
			}
		}
	}
	
	public static List<String> splitOnWords(String text, int charLimit) {
		List<String> output = new ArrayList<>();
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
