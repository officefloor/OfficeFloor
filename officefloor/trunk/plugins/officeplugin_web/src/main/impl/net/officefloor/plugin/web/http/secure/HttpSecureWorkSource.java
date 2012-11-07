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
package net.officefloor.plugin.web.http.secure;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link WorkSource} to provide appropriately secure
 * {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecureWorkSource extends AbstractWorkSource<HttpSecureTask> {

	/*
	 * ==================== WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement AbstractWorkSource<HttpSecureTask>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractWorkSource<HttpSecureTask>.loadSpecification");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpSecureTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {
		// TODO implement WorkSource<HttpSecureTask>.sourceWork
		throw new UnsupportedOperationException(
				"TODO implement WorkSource<HttpSecureTask>.sourceWork");
	}

}