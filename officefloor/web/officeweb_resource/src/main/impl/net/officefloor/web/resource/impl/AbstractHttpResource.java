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

package net.officefloor.web.resource.impl;

import java.io.Closeable;

import net.officefloor.web.resource.HttpResource;

/**
 * Abstract {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResource implements HttpResource, Closeable {

	/**
	 * Path.
	 */
	protected String path;

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            Resource path.
	 */
	public AbstractHttpResource(String path) {
		this.path = path;
	}

	/*
	 * ======================= HttpResource ==========================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public boolean equals(Object obj) {

		// Check if same object
		if (this == obj) {
			return true;
		}

		// Ensure same type
		if (!(obj instanceof AbstractHttpResource)) {
			return false;
		}
		AbstractHttpResource that = (AbstractHttpResource) obj;

		// Return whether same resource by path
		return this.path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}
