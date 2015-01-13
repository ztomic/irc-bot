package com.ztomic.ircbot.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;

@ConfigurationProperties(prefix = "irc", ignoreUnknownFields = false)
public class IrcConfiguration {
	
	private boolean quizEnabled = false;
	
	private boolean startIdent = true;
	
	private String secretMasterKey = "QuizBot";

	private List<ServerConfig> servers = new ArrayList<>();
	
	public boolean isQuizEnabled() {
		return quizEnabled;
	}
	
	public void setQuizEnabled(boolean quizEnabled) {
		this.quizEnabled = quizEnabled;
	}
	
	public boolean isStartIdent() {
		return startIdent;
	}
	
	public void setStartIdent(boolean startIdent) {
		this.startIdent = startIdent;
	}
	
	public String getSecretMasterKey() {
		return secretMasterKey;
	}
	
	public void setSecretMasterKey(String secretMasterKey) {
		this.secretMasterKey = secretMasterKey;
	}

	public List<ServerConfig> getServers() {
		return servers;
	}

	public void setServers(List<ServerConfig> servers) {
		this.servers = servers;
	}
	
	public ServerConfig getServer(String hostname) {
		for (ServerConfig sc : servers) {
			if (ObjectUtils.nullSafeEquals(sc.getHostname(), hostname)) {
				return sc;
			}
		}
		return null;
	}
	
	public ChannelConfig getChannel(String serverHostName, String channelName) {
		ServerConfig server = getServer(serverHostName);
		if (server != null) {
			return server.getChannel(channelName);
		}
		return null;
	}

	public static class ServerConfig {
		
		private String name = "Callidus";
		private String login = "callidus";
		private String realName = "${pom.artifactId} by ztomic, v${pom.version}";

		private boolean autoNickChange = true;
		private String hostname;
		private int port = 6667;
		private String nickServPassword;
		private String encoding = "WINDOWS-1250";
		private int maxLineLength = 380;
		private long messageLimit = 15;
		private long messageLimitInterval = 2;
		private boolean quiz = true;

		private List<ChannelConfig> channels = new ArrayList<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getRealName() {
			return realName;
		}

		public void setRealName(String realName) {
			this.realName = realName;
		}

		public boolean isAutoNickChange() {
			return autoNickChange;
		}

		public void setAutoNickChange(boolean autoNickChange) {
			this.autoNickChange = autoNickChange;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getNickServPassword() {
			return nickServPassword;
		}

		public void setNickServPassword(String nickServPassword) {
			this.nickServPassword = nickServPassword;
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public int getMaxLineLength() {
			return maxLineLength;
		}

		public void setMaxLineLength(int maxLineLength) {
			this.maxLineLength = maxLineLength;
		}

		public long getMessageLimit() {
			return messageLimit;
		}

		public void setMessageLimit(long messageLimit) {
			this.messageLimit = messageLimit;
		}

		public long getMessageLimitInterval() {
			return messageLimitInterval;
		}

		public void setMessageLimitInterval(long messageLimitInterval) {
			this.messageLimitInterval = messageLimitInterval;
		}
		
		public boolean isQuiz() {
			return quiz;
		}
		
		public void setQuiz(boolean quiz) {
			this.quiz = quiz;
		}

		public List<ChannelConfig> getChannels() {
			return channels;
		}

		public void setChannels(List<ChannelConfig> channels) {
			this.channels = channels;
		}
		
		public ChannelConfig getChannel(String name) {
			for (ChannelConfig c : channels) {
				if (ObjectUtils.nullSafeEquals(c.getName(), name)) {
					return c;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return "ServerConfig [name=" + name + ", login=" + login + ", realName=" + realName + ", autoNickChange=" + autoNickChange + ", hostname=" + hostname + ", port=" + port + ", nickServPassword=" + nickServPassword + ", encoding=" + encoding + ", maxLineLength=" + maxLineLength
					+ ", messageLimit=" + messageLimit + ", messageLimitInterval=" + messageLimitInterval + ", quiz=" + quiz + ", channels=" + channels + "]";
		}

	}

	public static class ChannelConfig {
		
		private String name = "#quiz";
		private String language = "CROATIAN";
		private String password;
		private boolean quiz = true;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public boolean isQuiz() {
			return quiz;
		}

		public void setQuiz(boolean quiz) {
			this.quiz = quiz;
		}

		@Override
		public String toString() {
			return "ChannelConfig [name=" + name + ", language=" + language + ", password=" + password + ", quiz=" + quiz + "]";
		}
	}

	@Override
	public String toString() {
		return "IrcConfiguration [servers=" + servers + "]";
	}
	
}
