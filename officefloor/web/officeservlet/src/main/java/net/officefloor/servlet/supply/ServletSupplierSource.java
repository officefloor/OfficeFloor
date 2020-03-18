package net.officefloor.servlet.supply;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link SupplierSource} to provide {@link Servlet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletSupplierSource extends AbstractSupplierSource {

	/**
	 * Obtains the {@link ServletManager}.
	 * 
	 * @return {@link ServletManager}.
	 */
	public static ServletManager getServletManager() {
		return servletManager.get();
	}

	/**
	 * {@link ServletManager} {@link ThreadLocal} to use while compiling.
	 */
	private static final ThreadLocal<ServletManager> servletManager = new ThreadLocal<>();

	/**
	 * {@link TomcatServletManager}.
	 */
	private final TomcatServletManager servletContainer;

	/**
	 * Instantiate.
	 * 
	 * @param servletContainer {@link TomcatServletManager}.
	 */
	public ServletSupplierSource(TomcatServletManager servletContainer) {
		this.servletContainer = servletContainer;
	}

	/*
	 * ================= SupplierSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Provide means to load dependencies for Servlets while compiling
		servletManager.set(this.servletContainer);
		context.addCompileCompletion((completion) -> {
			servletManager.remove();
		});
	}

	@Override
	public void terminate() {
		// Managed object stops Tomcat
	}

}