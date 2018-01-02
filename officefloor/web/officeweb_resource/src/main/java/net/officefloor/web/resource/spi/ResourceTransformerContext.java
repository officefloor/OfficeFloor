/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.file.Path;

import net.officefloor.server.http.HttpHeaderValue;

/**
 * Context for the {@link ResourceTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformerContext {

	/**
	 * Obtains the {@link Path} to the resource being transformed.
	 * 
	 * @return {@link Path} to the resource being transformed.
	 */
	Path getResource();

	/**
	 * <p>
	 * Creates a new file.
	 * <p>
	 * All files required for transform should be created via this method. This
	 * is to ensure the files are managed.
	 * 
	 * @return {@link Path} to the new file.
	 * @throws IOException
	 *             If fails to create the new file.
	 */
	Path createFile() throws IOException;

	/**
	 * Obtains the <code>Content-Encoding</code> of the resource.
	 * 
	 * @return <code>Content-Encoding</code> of the resource. May be
	 *         <code>null</code> if resource not yet encoded.
	 */
	String getContentEncoding();

	/**
	 * Specifies the <code>Content-Encoding</code> for the transformed resource.
	 * 
	 * @param contentEncoding
	 *            <code>Content-Encoding</code> for the transformed resource.
	 * @throws IOException
	 *             If <code>Content-Encoding</code> already specified by another
	 *             {@link ResourceTransformer}.
	 * 
	 * @see #getContentEncoding()
	 */
	void setContentEncoding(HttpHeaderValue contentEncoding) throws IOException;

	/**
	 * <p>
	 * Specifies the {@link Path} to the transformed resource.
	 * <p>
	 * This is optional to invoke. Should a transform not be applied, then this
	 * should not be invoked.
	 * 
	 * @param resource
	 *            {@link Path} to the transformed resource.
	 */
	void setTransformedResource(Path resource);

}