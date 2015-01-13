package com.ztomic.ircbot.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.IdentServer;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ztomic.ircbot.component.ExecutorFactory;
import com.ztomic.ircbot.component.pircbotx.CustomBotFactory;
import com.ztomic.ircbot.component.pircbotx.CustomMultiBotManager;
import com.ztomic.ircbot.component.pircbotx.CustomPircBotX;
import com.ztomic.ircbot.component.pircbotx.CustomThreadedListenerManager;
import com.ztomic.ircbot.configuration.IrcConfiguration;
import com.ztomic.ircbot.listener.quiz.QuizListener;

@Service
@EnableConfigurationProperties({IrcConfiguration.class})
public class IrcConnector {
	
	private static final Logger log = LoggerFactory.getLogger(IrcConnector.class);
	
	@Autowired
	private IrcConfiguration ircConfig;
	
	@Value("${pom.version}")
	private String version;
	
	@Autowired
	private List<ListenerAdapter<PircBotX>> listeners;
	
	@Autowired
	private ExecutorFactory executorFactory;

	@Bean
	public MultiBotManager<CustomPircBotX> createBot() throws IOException, IrcException {
		if (ircConfig.isStartIdent()) {
			IdentServer.startServer();
		}
		
		MultiBotManager<CustomPircBotX> multiBotManager = new CustomMultiBotManager();
		for (IrcConfiguration.ServerConfig server : ircConfig.getServers()) {
			Builder<PircBotX> builder = new Configuration.Builder<PircBotX>()
					.setBotFactory(new CustomBotFactory(server.getMessageLimit(), server.getMessageLimitInterval()))
					.setName(server.getName())
					.setLogin(server.getLogin())
					.setRealName(server.getRealName())
					.setVersion(version)
					.setAutoNickChange(server.isAutoNickChange())
					.setServer(server.getHostname(), server.getPort())
					.setMaxLineLength(server.getMaxLineLength())
					.setAutoSplitMessage(true)
					.setMessageDelay(0)
					.setEncoding(Charset.forName(server.getEncoding()))
					.setAutoReconnect(true);
			if (StringUtils.hasText(server.getNickServPassword())) {
				builder.setNickservPassword(server.getNickServPassword());
			}
			builder.setListenerManager(new CustomThreadedListenerManager(executorFactory.createPersistenceThreadPoolExecutor(Integer.MAX_VALUE)));
			
			for (IrcConfiguration.ChannelConfig channel : server.getChannels()) {
				if (StringUtils.hasLength(channel.getPassword())) {
					builder.addAutoJoinChannel(channel.getName(), channel.getPassword());
				} else {
					builder.addAutoJoinChannel(channel.getName());
				}
			}
			
			for (ListenerAdapter<PircBotX> listener : listeners) {
				if (server.isQuiz() || !(listener instanceof QuizListener)) {
					log.debug("Adding listener: {}",  listener);
					builder.addListener(listener);
				} else {
					log.debug("Skipping quiz listener: {}", listener);
				}
			}
			Configuration<PircBotX> configuration = builder.buildConfiguration();
			log.debug("Adding bot for {}", server);
			multiBotManager.addBot(new CustomPircBotX(configuration));
		}
		
		multiBotManager.start();
		
		log.info("All bots started.");

		return multiBotManager;
	}
}
