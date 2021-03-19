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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.supply.extension.BeforeCompleteServletSupplierExtensionContext;
import net.officefloor.servlet.supply.extension.ServletSupplierExtension;
import net.officefloor.servlet.supply.extension.ServletSupplierExtensionServiceFactory;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link SupplierSource} to provide {@link Servlet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} name to chain the {@link ServletManager} in to service
	 * {@link HttpRequest} instances via {@link Filter}/{@link Servlet} mappings.
	 */
	public static final String PROPERTY_CHAIN_SERVLETS = "chain.servlets";

	/**
	 * Obtains the {@link ServletManager}.
	 * 
	 * @return {@link ServletManager}.
	 */
	public static ServletManager getServletManager() {
		ServletSupplierSource servletSupplier = supplier.get();
		if (servletSupplier == null) {
			throw new IllegalStateException(ServletManager.class.getSimpleName() + " is not available. Please confirm "
					+ ServletWoofExtensionService.class.getName() + " is extending WoOF.");
		}
		return servletSupplier.servletContainer;
	}

	/**
	 * <p>
	 * Sends an error.
	 * <p>
	 * The {@link Throwable} will (in most cases) be reported back to
	 * {@link OfficeFloor} for appropriate {@link Exception} response. If not
	 * possible, an appropriate {@link Exception} message is also provided.
	 * 
	 * @param failure  Failure.
	 * @param request  {@link HttpServletRequest}.
	 * @param response {@link HttpServletResponse}.
	 * @throws IOException If fails to send error.
	 */
	public static void sendError(Throwable failure, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		// Register the failure to provide back to OfficeFloor
		request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, failure);

		// In case lost, also provide failure message
		response.sendError(500, failure.getMessage());
	}

	/**
	 * Registers the {@link Class} for injection.
	 * 
	 * @param className {@link Class} name.
	 * @throws Exception If fails to register for injection.
	 */
	public static void registerForInjection(String className) throws Exception {
		ServletSupplierSource source = supplier.get();
		Class<?> clazz = source.sourceContext.loadClass(className);
		source.injectionRegistry.registerForInjection(clazz, source.sourceContext);
	}

	/**
	 * Force starts the {@link Servlet} container.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @return {@link ServletServicer} to the {@link Servlet} container.
	 * @throws Exception If fails to start {@link Servlet} container.
	 */
	public static ServletServicer forceStartServletContainer(AvailableType[] availableTypes) throws Exception {

		// Attempt complete if not already completed
		ServletSupplierSource source = supplier.get();
		if (source != null) {

			// Complete to start Servlet container
			source.complete(availableTypes);

			// Return Servlet servicer
			return source.servletContainer;

		} else {
			// Already completed
			return null;
		}
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
	 * {@link ServletCompletion}.
	 */
	private ServletCompletion servletCompletion;

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
	 * Completes the loading of the {@link Servlet} container.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @throws Exception If fails to load {@link Servlet} container.
	 */
	private void complete(AvailableType[] availableTypes) throws Exception {

		// Determine if already completed
		if (this.servletCompletion == null) {
			return; // already completed
		}

		// Avoid recursive completion
		ServletCompletion completion = this.servletCompletion;
		this.servletCompletion = null;

		// Complete
		completion.complete(availableTypes);
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

		// Provide context for servlet manager
		this.servletContainer.setSupplierSourceContext(context);

		// Determine if chain in servlet manager
		boolean isChainServletManager = Boolean
				.parseBoolean(context.getProperty(PROPERTY_CHAIN_SERVLETS, Boolean.FALSE.toString()));
		if (isChainServletManager) {
			this.servletContainer.chainInServletManager();
		}

		// Load the extensions
		List<ServletSupplierExtension> extensions = new LinkedList<>();
		for (ServletSupplierExtension extension : context
				.loadOptionalServices(ServletSupplierExtensionServiceFactory.class)) {
			extensions.add(extension);
		}

		// Provide completion of Servlet container
		this.servletCompletion = new ServletCompletion(context);
		context.addCompileCompletion((completion) -> {

			// Run completion
			for (ServletSupplierExtension extension : extensions) {
				extension.beforeCompletion(new BeforeCompleteServletSupplierExtensionContext() {

					@Override
					public AvailableType[] getAvailableTypes() {
						return completion.getAvailableTypes();
					}
				});
			}

			// Complete
			AvailableType[] availableTypes = completion.getAvailableTypes();
			this.complete(availableTypes);

			// Remove, as further injection registration is ignored
			supplier.remove();
		});
	}

	@Override
	public void terminate() {
		// Managed object stops Tomcat
	}

	/**
	 * Completes loading the {@link Servlet} container.
	 */
	private class ServletCompletion {

		/**
		 * {@link SupplierSourceContext}.
		 */
		private final SupplierSourceContext context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link SupplierSourceContext}.
		 */
		private ServletCompletion(SupplierSourceContext context) {
			this.context = context;
		}

		/**
		 * Completes loading the {@link Servlet} container.
		 * 
		 * @param availableTypes {@link AvailableType} intances.
		 */
		private void complete(AvailableType[] availableTypes) throws Exception {

			// Start the container (so servlets are registered)
			ServletSupplierSource.this.servletContainer.start(availableTypes);

			// Add the managed object
			ServletServicerManagedObjectSource servletMos = new ServletServicerManagedObjectSource(
					ServletSupplierSource.this.servletContainer, ServletSupplierSource.this.injectionRegistry);
			this.context.addManagedObjectSource(null, ServletServicer.class, servletMos);
		}
	}

}
