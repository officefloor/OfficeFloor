package net.officefloor.servlet.supply;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.executor.ManagedObjectExecutorFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link ManagedObjectSource} to provide {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletManagerManagedObjectSource
		extends AbstractManagedObjectSource<None, ServletManagerManagedObjectSource.FlowKeys> implements ManagedObject {

	/**
	 * Flow keys.
	 */
	public static enum FlowKeys {
		EXECUTOR
	}

	/**
	 * {@link ManagedObjectExecutorFactory} for {@link AsyncContext}.
	 */
	private ManagedObjectExecutorFactory<FlowKeys> executorFactory;

	/**
	 * {@link TomcatServletManager}.
	 */
	private final TomcatServletManager servletManager;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * Instantiate.
	 * 
	 * @param servletManager {@link TomcatServletManager}.
	 */
	public ServletManagerManagedObjectSource(TomcatServletManager servletManager) {
		this.servletManager = servletManager;
	}

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, FlowKeys> context) throws Exception {

		// Specify meta-data
		context.setObjectClass(ServletServicer.class);

		// Provide async executor
		this.executorFactory = new ManagedObjectExecutorFactory<>(context, FlowKeys.EXECUTOR,
				AsyncContext.class.getSimpleName());
	}

	@Override
	public void start(ManagedObjectExecuteContext<FlowKeys> context) throws Exception {

		// Capture logger for possible stop failure
		this.logger = context.getLogger();

		// Create the executor
		Executor executor = this.executorFactory.createExecutor(context, this);

		// Start servlet container
		this.servletManager.init(executor);
		this.servletManager.start();
	}

	@Override
	public void stop() {
		try {
			this.servletManager.stop();
		} catch (Exception ex) {
			this.logger.log(Level.WARNING, "Failed to shutdown " + this.servletManager.getClass().getSimpleName(), ex);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================== ManagedObject ========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.servletManager;
	}

}