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

package net.officefloor.servlet;

import java.util.function.Consumer;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.FilterDef;

import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * Manager of {@link Servlet} instances for {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletManager {

	/**
	 * Obtains the {@link Context}.
	 * 
	 * @return {@link Context}.
	 */
	Context getContext();

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param name         Name of {@link Servlet}.
	 * @param servletClass {@link Servlet} {@link Class}.
	 * @param decorator    Decorates the {@link Servlet}. May be <code>null</code>.
	 * @return {@link ServletServicer}.
	 */
	ServletServicer addServlet(String name, Class<? extends Servlet> servletClass, Consumer<Wrapper> decorator);

	/**
	 * Adds a {@link Servlet} instance.
	 * 
	 * @param name                 Name of {@link Servlet}.
	 * @param servlet              {@link Servlet}.
	 * @param isInjectDependencies Flags to inject dependencies into the
	 *                             {@link Servlet} instance.
	 * @param decorator            Decorates the {@link Servlet}. May be
	 *                             <code>null</code>.
	 * @return {@link ServletServicer}.
	 */
	ServletServicer addServlet(String name, Servlet servlet, boolean isInjectDependencies, Consumer<Wrapper> decorator);

	/**
	 * Adds a {@link Filter}.
	 * 
	 * @param name        Name of {@link Filter}.
	 * @param filterClass {@link Filter} {@link Class}.
	 * @param decorator   Decorates the {@link Filter}. May be <code>null</code>.
	 * @return {@link FilterServicer}.
	 */
	FilterServicer addFilter(String name, Class<? extends Filter> filterClass, Consumer<FilterDef> decorator);

	/**
	 * <p>
	 * Obtains a dependency.
	 * <p>
	 * The dependency is via a {@link SupplierThreadLocal} that is always available
	 * in servicing a {@link Servlet} / {@link Filter}.
	 * 
	 * @param <T>       Type of dependency.
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @return Dependency.
	 */
	<T> T getDependency(String qualifier, Class<? extends T> type);

	/**
	 * <p>
	 * Obtains the {@link AvailableType} instances from {@link OfficeFloor}.
	 * <p>
	 * This should only be invoked during {@link Servlet} container startup.
	 * 
	 * @return {@link AvailableType} instances from {@link OfficeFloor}.
	 * @throws IllegalStateException If invoked before completion of
	 *                               {@link ServletSupplierSource}.
	 */
	AvailableType[] getAvailableTypes() throws IllegalStateException;

	/**
	 * Chains in this {@link ServletManager} to service HTTP requests. This allows
	 * the backing {@link Servlet} container to route requests to the appropriate
	 * {@link Filter} / {@link Servlet} to service the HTTP request.
	 */
	void chainInServletManager();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getSourceContext();

}
