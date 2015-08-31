package com.ztomic.ircbot.component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutorFactory {
	
	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
	public PersistenceThreadPoolExecutor createPersistenceThreadPoolExecutor(int poolSize) {
		PersistenceThreadPoolExecutor executor = new PersistenceThreadPoolExecutor(0, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
		executor.setEntityManagerFactory(entityManagerFactory);
		return executor;
	}
	
}
