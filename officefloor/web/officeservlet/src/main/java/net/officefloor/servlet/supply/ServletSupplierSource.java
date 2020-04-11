/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet.supply;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.supplier.source.SupplierCompileContext;
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
public class ServletSupplierSource extends AbstractSupplierSource implements ServletConfigurationInterest {

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
	 * Registers a {@link ServletConfigurationInterest}.
	 */
	public static ServletConfigurationInterest registerInterest() {

		// Obtain the supplier source
		ServletSupplierSource source = supplier.get();

		// Increment interest
		source.interestCount++;

		// Return source
		return source;
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
	 * {@link ServletConfigurationInterest} count.
	 */
	private int interestCount = 0;

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

		// Make available immediately (available to all suppliers)
		supplier.set(this);
	}

	/**
	 * Possibly completes the {@link ServletConfigurationInterest}.
	 * 
	 * @param context {@link SupplierCompileContext}.
	 * @throws Exception If fails to complete {@link ServletConfigurationInterest}.
	 */
	public void completeInterest(SupplierCompileContext context) throws Exception {

		// Decrement interests as complete
		this.interestCount--;

		// Determine if further configuration
		if (this.interestCount > 0) {
			return;
		}

		// Start the container (so servlets are registered)
		this.servletContainer.start();
		
		// Remove, as further injection registration is ignored
		supplier.remove();
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

		// Register interest in configuration until complete
		this.interestCount++;

		// Provide means to load dependencies for Servlets while compiling
		context.addCompileCompletion((completion) -> {

			// Add the managed object
			ServletServicerManagedObjectSource servletMos = new ServletServicerManagedObjectSource(
					this.servletContainer, this.injectionRegistry);
			context.addManagedObjectSource(null, ServletServicer.class, servletMos);

			// Completes the interest
			this.completeInterest(completion);
		});
	}

	@Override
	public void terminate() {
		// Managed object stops Tomcat
	}

	/*
	 * ============= ServletConfigurationInterest =============
	 */

	@Override
	public void completeInterest() throws Exception {
		this.completeInterest(this.sourceContext);
	}

}
