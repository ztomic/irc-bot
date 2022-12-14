package com.ztomic.ircbot.component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecutorFactory {
	
	private final EntityManagerFactory entityManagerFactory;

	public ExecutorFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public PersistenceThreadPoolExecutor createPersistenceThreadPoolExecutor(String namePrefix, int poolSize) {
		PersistenceThreadPoolExecutor executor = new PersistenceThreadPoolExecutor(0, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new CustomizableThreadFactory(namePrefix + "-"));
		executor.setEntityManagerFactory(entityManagerFactory);
		return executor;
	}
	
}
