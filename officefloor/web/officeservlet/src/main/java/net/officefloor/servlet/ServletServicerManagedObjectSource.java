package net.officefloor.servlet;

import java.util.logging.Level;

import javax.servlet.Servlet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.servlet.tomcat.TomcatServletServicer;

/**
 * {@link ManagedObjectSource} to provide {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServicerManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<None> executeContext;

	/**
	 * {@link TomcatServletServicer}.
	 */
	private TomcatServletServicer servletContainer;

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(ServletServicer.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Create and start the embedded servlet container
		this.servletContainer = new TomcatServletServicer("/");
		this.servletContainer.start();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletContainerManagedObject();
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
	 * {@link Servlet} container {@link ManagedObject}.
	 */
	private class ServletContainerManagedObject implements ManagedObject {

		/*
		 * ================== ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return ServletServicerManagedObjectSource.this.servletContainer;
		}
	}

}