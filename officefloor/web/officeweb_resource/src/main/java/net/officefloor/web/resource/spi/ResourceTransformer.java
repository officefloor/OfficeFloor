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
import java.nio.file.Path;

/**
 * Transforms resource instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformer {

	/**
	 * <p>
	 * Transforms the resource at {@link Path} to another resource at the return
	 * {@link Path}.
	 * <p>
	 * Typically this is to compress the files, however available for other
	 * transforms to files (such as altering contents of files).
	 * 
	 * @param context
	 *            {@link ResourceTransformerContext}.
	 * @throws IOException
	 *             If fails to transform the resource.
	 */
	void transform(ResourceTransformerContext context) throws IOException;

}
