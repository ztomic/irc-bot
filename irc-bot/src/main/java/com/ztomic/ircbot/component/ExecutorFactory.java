package com.ztomic.ircbot.component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutorFactory {
	
	private final EntityManagerFactory entityManagerFactory;

	@Autowired
	public ExecutorFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public PersistenceThreadPoolExecutor createPersistenceThreadPoolExecutor(String namePrefix, int poolSize) {
		final AtomicInteger handlerCount = new AtomicInteger();
		PersistenceThreadPoolExecutor executor = new PersistenceThreadPoolExecutor(0, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
			Thread t = new Thread(r);
			t.setName(namePrefix + "-" + handlerCount.incrementAndGet());
			return t;
		});
		executor.setEntityManagerFactory(entityManagerFactory);
		return executor;
	}
	
}
