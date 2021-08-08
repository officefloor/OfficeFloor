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

/**
 * Factory for the creation of {@link HttpObjectParser} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectParserFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectParser} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectParser} for the {@link Object}.
	 * 
	 * @param <T>
	 *            Object type.
	 * @param objectClass
	 *            {@link Object} {@link Class}.
	 * @return {@link HttpObjectParser} for the {@link Object}. May return
	 *         <code>null</code> if does not support parsing out the particular
	 *         {@link Object}.
	 * @throws Exception
	 *             If fails to create the {@link HttpObjectParser} for the
	 *             {@link Object}.
	 */
	<T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception;

}
