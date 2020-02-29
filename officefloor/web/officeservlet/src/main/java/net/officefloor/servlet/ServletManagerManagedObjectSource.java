package net.officefloor.servlet;

import java.util.logging.Level;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link ManagedObjectSource} to provide {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletManagerManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<None> executeContext;

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

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
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Capture class loader
		this.classLoader = context.getManagedObjectSourceContext().getClassLoader();

		// Specify meta-data
		context.setObjectClass(ServletManager.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Create and start the embedded servlet container
		this.servletContainer = new TomcatServletManager("/", this.classLoader);
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