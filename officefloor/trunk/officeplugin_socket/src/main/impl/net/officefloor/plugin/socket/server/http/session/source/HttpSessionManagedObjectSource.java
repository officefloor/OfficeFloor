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
package net.officefloor.plugin.socket.server.http.session.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * {@link ManagedObjectSource} for a {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, Indexed> {

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO Implement
		// AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadSpecification
		throw new UnsupportedOperationException(
				"AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
			throws Exception {
		// TODO Implement
		// AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadMetaData
		throw new UnsupportedOperationException(
				"AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadMetaData");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO Implement
		// AbstractManagedObjectSource<Indexed,Indexed>.getManagedObject
		throw new UnsupportedOperationException(
				"AbstractManagedObjectSource<Indexed,Indexed>.getManagedObject");
	}

}