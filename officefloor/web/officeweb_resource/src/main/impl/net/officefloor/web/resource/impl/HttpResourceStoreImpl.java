/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.resource.impl;

import java.io.Closeable;
import java.io.IOException;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceTransformer;

/**
 * {@link HttpResourceStore} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceStoreImpl implements HttpResourceStore, Closeable {

	public HttpResourceStoreImpl(String location, ResourceSystemFactory resourceSystemFactory,
			FileCacheFactory fileRepositoryFactory, ResourceTransformer[] transformers,
			String[] directoryDefaultResourceNames) {
	}

	/**
	 * Obtains the {@link HttpResourceCache}.
	 * 
	 * @return {@link HttpResourceCache}.
	 */
	public HttpResourceCache getCache() {
		return null;
	}

	/*
	 * ==================== HttpResourceStore ======================
	 */

	@Override
	public HttpResource getHttpResource(String path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * ==================== Closeable ======================
	 */

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}