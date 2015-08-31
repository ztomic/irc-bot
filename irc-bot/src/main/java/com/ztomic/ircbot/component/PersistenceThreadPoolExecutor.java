package com.ztomic.ircbot.component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Executor koji ima slicnu funkcionalnost kao
 * {@link OpenEntityManagerInViewInterceptor}, odnosno
 * {@link OpenEntityManagerInViewFilter} a to je da prije pozivanja nekog
 * {@link Runnable} ili {@link Callable} kreira {@link EntityManager} ukoliko
 * nije kreiran, binda ga na thread i nakon izvrsavanja ga zatvara.
 * 
 * Primjer koristenja:
 * 
 * <pre>
 * <code>
 * EntityManagerFactory emf = module.getSpringBean(EntityManagerFactory.class); // ili neki drugi nacin dohvacanja EMF-a
 * PersistenceThreadPoolExecutor executor = new PersistenceThreadPoolExecutor(1, 10, 0, TimeUnit.SECONDS, new SynchronousQueue&lt;Runnable&gt;());
 * executor.setEntityManagerFactory(emf);
 * executor.execute(new Runnable() {	
 *    &#64;Override
 *    public void run() {
 *      AccountRepository repo = PersistenceContext.getRepositoryFor(com.nth.payment.manager.entity.Account.class);
 *      for (com.nth.payment.manager.entity.Account account : repo.findAll()) {
 *        for (PaymentService service : account.getServices()) {
 *          for (PaymentProduct product : service.getPaymentProducts()) {
 *            for (PaymentProductRoute route : product.getPaymentProductRoutes()) {
 *              // do something
 *            }
 *          }
 *        }
 *      }
 *    }
 * });
 * </code>
 * </pre>
 */
public class PersistenceThreadPoolExecutor extends ThreadPoolExecutor {

	private static Logger log = LoggerFactory.getLogger(PersistenceThreadPoolExecutor.class);

	private ThreadLocal<Boolean> participate = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	private EntityManagerFactory entityManagerFactory;

	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial
	 * parameters and default thread factory and rejected execution handler. It
	 * may be more convenient to use one of the {@link Executors} factory
	 * methods instead of this general purpose constructor.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize
	 *            the maximum number of threads to allow in the pool
	 * @param keepAliveTime
	 *            when the number of threads is greater than the core, this is
	 *            the maximum time that excess idle threads will wait for new
	 *            tasks before terminating.
	 * @param unit
	 *            the time unit for the {@code keepAliveTime} argument
	 * @param workQueue
	 *            the queue to use for holding tasks before they are executed.
	 *            This queue will hold only the {@code Runnable} tasks submitted
	 *            by the {@code execute} method.
	 * @throws IllegalArgumentException
	 *             if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException
	 *             if {@code workQueue} is null
	 */
	public PersistenceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial
	 * parameters and default rejected execution handler.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize
	 *            the maximum number of threads to allow in the pool
	 * @param keepAliveTime
	 *            when the number of threads is greater than the core, this is
	 *            the maximum time that excess idle threads will wait for new
	 *            tasks before terminating.
	 * @param unit
	 *            the time unit for the {@code keepAliveTime} argument
	 * @param workQueue
	 *            the queue to use for holding tasks before they are executed.
	 *            This queue will hold only the {@code Runnable} tasks submitted
	 *            by the {@code execute} method.
	 * @param threadFactory
	 *            the factory to use when the executor creates a new thread
	 * @throws IllegalArgumentException
	 *             if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException
	 *             if {@code workQueue} or {@code threadFactory} is null
	 */
	public PersistenceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial
	 * parameters and default thread factory.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize
	 *            the maximum number of threads to allow in the pool
	 * @param keepAliveTime
	 *            when the number of threads is greater than the core, this is
	 *            the maximum time that excess idle threads will wait for new
	 *            tasks before terminating.
	 * @param unit
	 *            the time unit for the {@code keepAliveTime} argument
	 * @param workQueue
	 *            the queue to use for holding tasks before they are executed.
	 *            This queue will hold only the {@code Runnable} tasks submitted
	 *            by the {@code execute} method.
	 * @param handler
	 *            the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException
	 *             if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException
	 *             if {@code workQueue} or {@code handler} is null
	 */
	public PersistenceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial
	 * parameters.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize
	 *            the maximum number of threads to allow in the pool
	 * @param keepAliveTime
	 *            when the number of threads is greater than the core, this is
	 *            the maximum time that excess idle threads will wait for new
	 *            tasks before terminating.
	 * @param unit
	 *            the time unit for the {@code keepAliveTime} argument
	 * @param workQueue
	 *            the queue to use for holding tasks before they are executed.
	 *            This queue will hold only the {@code Runnable} tasks submitted
	 *            by the {@code execute} method.
	 * @param threadFactory
	 *            the factory to use when the executor creates a new thread
	 * @param handler
	 *            the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException
	 *             if one of the following holds:<br>
	 *             {@code corePoolSize < 0}<br>
	 *             {@code keepAliveTime < 0}<br>
	 *             {@code maximumPoolSize <= 0}<br>
	 *             {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException
	 *             if {@code workQueue} or {@code threadFactory} or
	 *             {@code handler} is null
	 */
	public PersistenceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	/**
	 * Set the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 * 
	 * @see javax.persistence.EntityManagerFactory#createEntityManager()
	 * @see javax.persistence.EntityManagerFactory#createEntityManager(java.util.Map)
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		log.debug("Using EntityManagerFactory: {}", emf);
		this.entityManagerFactory = emf;
	}

	/**
	 * Return the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return this.entityManagerFactory;
	}

	/**
	 * Create a JPA EntityManager to be bound to a request.
	 * <p>
	 * Can be overridden in subclasses.
	 * 
	 * @param emf
	 *            the EntityManagerFactory to use
	 * @see javax.persistence.EntityManagerFactory#createEntityManager()
	 */
	protected EntityManager createEntityManager(EntityManagerFactory emf) {
		return emf.createEntityManager();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (entityManagerFactory == null) {
			log.warn("EntityManagerFactory is null. Cannot bind EntityManager!");
			return;
		}
		if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
			// Do not modify the EntityManager: just set the participate flag.
			participate.set(true);
		} else {
			participate.set(false);
			log.debug("Opening JPA EntityManager in {}", getClass().getSimpleName());
			try {
				EntityManager em = createEntityManager(entityManagerFactory);
				EntityManagerHolder emHolder = new EntityManagerHolder(em);
				TransactionSynchronizationManager.bindResource(entityManagerFactory, emHolder);
			} catch (PersistenceException ex) {
				throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
			}
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (entityManagerFactory == null) {
			log.warn("EntityManagerFactory is null. Cannot unbind EntityManager!");
			return;
		}
		if (!participate.get()) {
			EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(entityManagerFactory);
			log.debug("Closing JPA EntityManager in {}", getClass().getSimpleName());
			EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
		}
	}

}
