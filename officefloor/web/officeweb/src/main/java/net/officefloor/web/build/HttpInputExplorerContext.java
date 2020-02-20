/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.compile.spi.office.ExecutionExplorerContext;
import net.officefloor.server.http.HttpMethod;

/**
 * Context for the {@link HttpInputExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInputExplorerContext extends ExecutionExplorerContext {

	/**
	 * Indicates if secure.
	 * 
	 * @return <code>true</code> if secure.
	 */
	boolean isSecure();

	/**
	 * {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getHttpMethod();

	/**
	 * Obtains the possible context path.
	 * 
	 * @return Context path. May be <code>null</code>.
	 */
	String getContextPath();

	/**
	 * Obtains the {@link HttpInput} path minus the context path.
	 * 
	 * @return {@link HttpInput} path minus the context path.
	 */
	String getRoutePath();

	/**
	 * Application path (includes context path).
	 * 
	 * @return Application path.
	 */
	String getApplicationPath();

	/**
	 * Obtains the {@link HttpObjectParserFactory} instances.
	 * 
	 * @return {@link HttpObjectParserFactory} instances.
	 */
	HttpObjectParserFactory[] getHttpObjectParserFactories();

	/**
	 * Obtains the {@link HttpObjectResponderFactory} instances.
	 * 
	 * @return {@link HttpObjectResponderFactory} instances.
	 */
	HttpObjectResponderFactory[] getHttpObjectResponderFactories();

	/**
	 * Obtains the documentation describing the {@link HttpInput}.
	 * 
	 * @return Documentation describing the {@link HttpInput}.
	 */
	String getDocumentation();

}
