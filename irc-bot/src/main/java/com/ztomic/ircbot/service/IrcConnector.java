package com.ztomic.ircbot.service;

import java.nio.charset.Charset;
import java.util.List;

import com.ztomic.ircbot.component.ExecutorFactory;
import com.ztomic.ircbot.component.pircbotx.CustomBotFactory;
import com.ztomic.ircbot.component.pircbotx.CustomThreadedListenerManager;
import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.listener.quiz.QuizListener;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.IdentServer;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IrcConnector {
	
	private static final Logger log = LoggerFactory.getLogger(IrcConnector.class);

	private final IrcConfiguration ircConfig;
	private final ExecutorFactory executorFactory;
	private final String version;
	private final List<ListenerAdapter> listeners;

	public IrcConnector(IrcConfiguration ircConfig, ExecutorFactory executorFactory, @Value("${pom.version}") String version, List<ListenerAdapter> listeners) {
		this.ircConfig = ircConfig;
		this.executorFactory = executorFactory;
		this.version = version;
		this.listeners = listeners;
	}

	@Bean
	public MultiBotManager createBot() {
		if (ircConfig.isStartIdent()) {
			IdentServer.startServer();
		}
		
		MultiBotManager multiBotManager = new MultiBotManager();
		for (IrcConfiguration.ServerConfig server : ircConfig.getServers()) {
			if (!server.isEnabled()) {
				log.debug("Skipping disabled server {}", server);
				continue;
			}
			Builder builder = new Configuration.Builder()
					.setBotFactory(new CustomBotFactory(server.getMessageLimit(), server.getMessageLimitInterval()))
					.setName(server.getName())
					.setLogin(server.getLogin())
					.setRealName(server.getRealName())
					.setVersion(version)
					.setAutoNickChange(server.isAutoNickChange())
					.addServer(server.getHostname(), server.getPort())
					.setMaxLineLength(server.getMaxLineLength())
					.setAutoSplitMessage(true)
					.setMessageDelay(0)
					.setEncoding(Charset.forName(server.getEncoding()))
					.setAutoReconnect(true);
			if (StringUtils.hasText(server.getNickServPassword())) {
				builder.setNickservPassword(server.getNickServPassword());
			}
			builder.setListenerManager(new CustomThreadedListenerManager(executorFactory.createPersistenceThreadPoolExecutor("server-handler-" + server.getName(), Integer.MAX_VALUE)));
			
			for (IrcConfiguration.ChannelConfig channel : server.getChannels()) {
				if (StringUtils.hasLength(channel.getPassword())) {
					builder.addAutoJoinChannel(channel.getName(), channel.getPassword());
				} else {
					builder.addAutoJoinChannel(channel.getName());
				}
			}
			
			for (ListenerAdapter listener : listeners) {
				if (server.isQuiz() || !(listener instanceof QuizListener)) {
					log.debug("Adding listener: {}",  listener);
					builder.addListener(listener);
				} else {
					log.debug("Skipping quiz listener: {}", listener);
				}
			}
			Configuration configuration = builder.buildConfiguration();
			log.debug("Adding bot for {}", server);
			multiBotManager.addBot(new PircBotX(configuration));
		}
		
		multiBotManager.start();
		
		log.info("All bots started.");

		return multiBotManager;
	}
}
