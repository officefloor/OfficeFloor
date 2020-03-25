package net.officefloor.servlet.supply;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.InjectionRegistry;
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
		source.injectionRegistry.registerForInjection(clazz, source.sourceContext);
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
	 * {@link InjectionRegistry}.
	 */
	private final InjectionRegistry injectionRegistry;

	/**
	 * {@link SupplierSourceContext}.
	 */
	private SupplierSourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param servletContainer  {@link TomcatServletManager}.
	 * @param injectionRegistry {@link InjectionRegistry}.
	 */
	public ServletSupplierSource(TomcatServletManager servletContainer, InjectionRegistry injectionRegistry) {
		this.servletContainer = servletContainer;
		this.injectionRegistry = injectionRegistry;
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
					this.servletContainer, this.injectionRegistry);
			completion.addManagedObjectSource(null, ServletServicer.class, servletMos);
		});
	}

	@Override
	public void terminate() {
		// Managed object stops Tomcat
	}

}