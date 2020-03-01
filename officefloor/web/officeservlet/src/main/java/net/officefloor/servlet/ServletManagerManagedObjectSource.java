package net.officefloor.servlet;

import java.util.concurrent.Executor;
import java.util.logging.Level;

import javax.servlet.AsyncContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.executor.ManagedObjectExecutorFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link ManagedObjectSource} to provide {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletManagerManagedObjectSource
		extends AbstractManagedObjectSource<None, ServletManagerManagedObjectSource.FlowKeys> {

	/**
	 * Flow keys.
	 */
	public static enum FlowKeys {
		EXECUTOR
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<FlowKeys> executeContext;

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link ManagedObjectExecutorFactory} for {@link AsyncContext}.
	 */
	private ManagedObjectExecutorFactory<FlowKeys> executorFactory;

	/**
	 * {@link TomcatServletManager}.
	 */
	private TomcatServletManager servletContainer;

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, FlowKeys> context) throws Exception {

		// Capture class loader
		this.classLoader = context.getManagedObjectSourceContext().getClassLoader();

		// Provide async executor
		this.executorFactory = new ManagedObjectExecutorFactory<>(context, FlowKeys.EXECUTOR,
				AsyncContext.class.getSimpleName());

		// Specify meta-data
		context.setObjectClass(ServletManager.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<FlowKeys> context) throws Exception {

		// Create the executor
		Executor executor = this.executorFactory.createExecutor(context, new ServletManagerManagedObject());

		// Create and start the embedded servlet container
		this.servletContainer = new TomcatServletManager("/", this.classLoader, executor);
		this.servletContainer.start();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletManagerManagedObject();
	}

	@Override
	public void stop() {
		// Stop the servlet container
		try {
			this.servletContainer.stop();
		} catch (Exception ex) {
			this.executeContext.getLogger().log(Level.WARNING, "Failed to shutdown Servlet container", ex);
		}
	}

	/**
	 * {@link ServletManager} {@link ManagedObject}.
	 */
	private class ServletManagerManagedObject implements ManagedObject {

		/*
		 * ================== ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return ServletManagerManagedObjectSource.this.servletContainer;
		}
	}

}