package net.officefloor.servlet.supply;

import java.util.Map;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.ServletInjector;
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
		return supplier.get().servletContainer;
	}

	/**
	 * Registers the {@link Class} for injection.
	 * 
	 * @param className {@link Class} name.
	 */
	public static void registerForInjection(String className) {
		ServletSupplierSource source = supplier.get();
		Class<?> clazz = source.sourceContext.loadClass(className);
		source.injectors.put(clazz, new ServletInjector(clazz, source.sourceContext));
	}

	/**
	 * {@link ServletManager} {@link ThreadLocal} to use while compiling.
	 */
	private static final ThreadLocal<ServletSupplierSource> supplier = new ThreadLocal<>();

	/**
	 * {@link TomcatServletManager}.
	 */
	private final TomcatServletManager servletContainer;

	/**
	 * {@link Class} to its {@link ServletInjector}.
	 */
	private final Map<Class<?>, ServletInjector> injectors;

	/**
	 * {@link SupplierSourceContext}.
	 */
	private SupplierSourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param servletContainer {@link TomcatServletManager}.
	 * @param injectors        {@link Class} to its {@link ServletInjector}.
	 */
	public ServletSupplierSource(TomcatServletManager servletContainer, Map<Class<?>, ServletInjector> injectors) {
		this.servletContainer = servletContainer;
		this.injectors = injectors;
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
		this.sourceContext = context;

		// Provide means to load dependencies for Servlets while compiling
		supplier.set(this);
		context.addCompileCompletion((completion) -> {

			// Start the container (so servlets are registered)
			this.servletContainer.start();

			// Remove, as further injection registration is ignored
			supplier.remove();

			// Add the managed object
			ServletServicerManagedObjectSource servletMos = new ServletServicerManagedObjectSource(
					this.servletContainer, this.injectors);
			completion.addManagedObjectSource(null, ServletServicer.class, servletMos);
		});
	}

	@Override
	public void terminate() {
		// Managed object stops Tomcat
	}

}