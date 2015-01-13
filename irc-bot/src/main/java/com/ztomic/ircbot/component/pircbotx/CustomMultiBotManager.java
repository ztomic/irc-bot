package com.ztomic.ircbot.component.pircbotx;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.pircbotx.MultiBotManager;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class CustomMultiBotManager extends MultiBotManager<CustomPircBotX> {
	
	private static final Logger log = LoggerFactory.getLogger(CustomMultiBotManager.class);

	public CustomMultiBotManager() {
		super();
	}
	
	public CustomMultiBotManager(ExecutorService botPool) {
		super(botPool);
	}
	
	@Override
	protected ListenableFuture<Void> startBot(CustomPircBotX bot) {
		checkNotNull(bot, "Bot cannot be null");
		ListenableFuture<Void> future = botPool.submit(new BotRunner(bot));
		synchronized (runningBotsLock) {
			runningBots.put(bot, future);
			runningBotsNumbers.put(bot, bot.getBotId());
		}
		Futures.addCallback(future, new BotFutureCallback(bot));
		return future;
	}
	
	
	protected class BotRunner implements Callable<Void> {
		protected final CustomPircBotX bot;
		
		public BotRunner(CustomPircBotX bot) {
			this.bot = bot;
		}

		public Void call() throws IOException, IrcException {
			Thread.currentThread().setName("botPool" + managerNumber + "-bot" + bot.getBotId());
			
			if (bot.getConfiguration().isAutoReconnect()) {
				do {
					try {
						bot.startBot();
					} catch (Throwable t) {
						log.debug("Error starting bot.", t);
						try {
							Thread.sleep(TimeUnit.SECONDS.toMillis(5));
						} catch (InterruptedException e) {
							//
						}
					}
				} while (bot.getConfiguration().isAutoReconnect() && !bot.isReconnectStopped());
			} else {
				bot.startBot();
			}

			return null;
		}
	}
	
}
