/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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