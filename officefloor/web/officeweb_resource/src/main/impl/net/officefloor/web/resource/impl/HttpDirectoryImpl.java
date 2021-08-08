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

import java.io.IOException;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link HttpDirectory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpDirectoryImpl extends AbstractHttpResource implements HttpDirectory {

	/**
	 * {@link HttpResourceStore}.
	 */
	private final HttpResourceStore store;

	/**
	 * Instantiate.
	 * 
	 * @param path  Path to the {@link HttpDirectory}.
	 * @param store {@link HttpResourceStore}.
	 */
	public HttpDirectoryImpl(String path, HttpResourceStore store) {
		super(path);
		this.store = store;
	}

	/*
	 * ================= HttpDirectory =====================
	 */

	@Override
	public boolean isExist() {
		return true;
	}

	@Override
	public HttpFile getDefaultHttpFile() throws IOException {
		return this.store.getDefaultHttpFile(this);
	}

	/*
	 * ===================== Closeable ==========================
	 */

	@Override
	public void close() throws IOException {
	}

}
