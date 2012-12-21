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
package net.officefloor.plugin.web.http.template.section;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * {@link WorkSource} to provide the {@link HttpTemplateInitialTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialWorkSource extends
		AbstractWorkSource<HttpTemplateInitialTask> {

	/**
	 * Property name for the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI;
	
	/*
	 * ======================= WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractWorkSource<HttpTemplateInitialTask>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractWorkSource<HttpTemplateInitialTask>.loadSpecification");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpTemplateInitialTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {
		// TODO implement WorkSource<HttpTemplateInitialTask>.sourceWork
		throw new UnsupportedOperationException(
				"TODO implement WorkSource<HttpTemplateInitialTask>.sourceWork");
	}

}