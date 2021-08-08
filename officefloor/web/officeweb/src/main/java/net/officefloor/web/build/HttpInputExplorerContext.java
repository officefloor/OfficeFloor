/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
