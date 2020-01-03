/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * Context for the {@link ResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystemContext {

	/**
	 * <p>
	 * Obtains the location for the {@link ResourceSystem}.
	 * <p>
	 * The location is a free text value that is interpreted specific to the
	 * {@link ResourceSystem} implementation. For example:
	 * <ul>
	 * <li>path to a directory of a file system</li>
	 * <li>class path prefix</li>
	 * <li>etc</li>
	 * </ul>
	 * 
	 * @return Location for the {@link ResourceSystem}.
	 */
	String getLocation();

	/**
	 * Obtains the directory default resource names.
	 * 
	 * @return Directory default resource names.
	 */
	String[] getDirectoryDefaultResourceNames();

	/**
	 * Obtains the {@link HttpResourceStore}.
	 * 
	 * @return {@link HttpResourceStore}.
	 */
	HttpResourceStore getHttpResourceStore();

	/**
	 * <p>
	 * Creates a new file.
	 * <p>
	 * All files required should be created via this method. This is to ensure the
	 * files are managed.
	 * 
	 * @param name Name to aid in identifying the file for debugging.
	 * @return {@link Path} to the new file.
	 * @throws IOException If fails to create the new file.
	 */
	Path createFile(String name) throws IOException;

	/**
	 * <p>
	 * Creates a new directory.
	 * <p>
	 * All directories should be created via this method. THis is to ensure the
	 * directories are managed.
	 * 
	 * @param name Name to aid in identifying the directory for debugging.
	 * @return {@link Path} to the new directory.
	 * @throws IOException If fails to create the new directory.
	 */
	Path createDirectory(String name) throws IOException;

	/**
	 * <p>
	 * Specifies the {@link Charset} for files within the {@link ResourceSystem}.
	 * <p>
	 * This is optional to invoke. If not configured (or configured with
	 * <code>null</code>) then the {@link Charset#defaultCharset()} will be used as
	 * the {@link Charset} for the files.
	 * 
	 * @param charset {@link Charset} of the files within the
	 *                {@link ResourceSystem}.
	 */
	void setCharset(Charset charset);

	/**
	 * <p>
	 * Allows the {@link ResourceSystem} to notify that a resource has changed.
	 * <p>
	 * The {@link HttpResourceStore} will then discard the {@link HttpResource} for
	 * the path. Should the path be <code>null</code> then all {@link HttpResource}
	 * instances are discarded.
	 * 
	 * @param resourcePath Path for the resource. If <code>null</code> then all
	 *                     resources will be discarded.
	 */
	void notifyResourceChanged(String resourcePath);

}
