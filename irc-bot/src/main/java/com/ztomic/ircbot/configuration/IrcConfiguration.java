package com.ztomic.ircbot.configuration;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;

@Data
@ConfigurationProperties(prefix = "irc", ignoreUnknownFields = false)
public class IrcConfiguration {
	
	private boolean quizEnabled = false;
	
	private boolean startIdent = true;
	
	private String secretMasterKey = "QuizBot";

	private List<ServerConfig> servers = new ArrayList<>();
	
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

	@Data
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
		private boolean enabled = true;

		private List<ChannelConfig> channels = new ArrayList<>();
		
		public ChannelConfig getChannel(String name) {
			for (ChannelConfig c : channels) {
				if (ObjectUtils.nullSafeEquals(c.getName(), name)) {
					return c;
				}
			}
			return null;
		}

	}

	@Data
	public static class ChannelConfig {
		
		private String name = "#quiz";
		private String language = "CROATIAN";
		private String password;
		private boolean quiz = true;
		
	}
	
}
