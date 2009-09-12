/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file.source;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.file.HttpFile;

/**
 * {@link WorkSource} to locate a {@link HttpFile} on the class path.
 *
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileLocatorWorkSource extends
		AbstractWorkSource<HttpFileLocatorTask> {

	/**
	 * Property to obtain the class path prefix on the request URI path to
	 * locate the {@link HttpFile}.
	 */
	public static final String PROPERTY_CLASSPATH_PREFIX = "classpath.prefix";

	/**
	 * Property to obtain the default file name should the request URI path
	 * resolve to a directory.
	 */
	public static final String PROPERTY_DEFAULT_FILE_NAME = "default.file.name";

	/*
	 * ==================== AbstractWorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASSPATH_PREFIX);
		context.addProperty(PROPERTY_DEFAULT_FILE_NAME);
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpFileLocatorTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {
		// TODO Implement WorkSource<Work>.sourceWork
		throw new UnsupportedOperationException("WorkSource<Work>.sourceWork");
	}

}