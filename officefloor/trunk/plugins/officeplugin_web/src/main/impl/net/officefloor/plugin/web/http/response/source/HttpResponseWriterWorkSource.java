/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.response.source;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactoryImpl;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseWriterWork;

/**
 * {@link WorkSource} to write the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterWorkSource extends
		AbstractWorkSource<HttpResponseWriterWork> {

	/*
	 * ======================== AbstractWorkSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpResponseWriterWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the HTTP response writer factory
		HttpResponseWriterFactory writerFactory = new HttpResponseWriterFactoryImpl();

		// Provide work type information
		workTypeBuilder
				.setWorkFactory(new HttpResponseWriterWork(writerFactory));

		// Provide the HTTP file writer task
		HttpFileWriterTaskFactory.addTaskType("WriteFileToResponse", workTypeBuilder);
	}

}