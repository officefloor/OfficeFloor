/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource;

/**
 * HTTP resource.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResource {

	/**
	 * <p>
	 * Obtains the path to this {@link HttpResource}.
	 * <p>
	 * The path is canonical to allow using it as a key for caching this
	 * {@link HttpResource}.
	 * 
	 * @return Canonical path to this {@link HttpResource}.
	 */
	String getPath();

	/**
	 * <p>
	 * Indicates if this {@link HttpResource} exists. Should this
	 * {@link HttpResource} not exist, only the path will be available.
	 * <p>
	 * This allows for caching of {@link HttpResource} instances not existing.
	 * 
	 * @return <code>true</code> if this {@link HttpResource} exists.
	 */
	boolean isExist();

}
