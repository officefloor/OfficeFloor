/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Factory for the creation of {@link RawManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagedObjectMetaDataFactory {

	/**
	 * Creates the {@link RawManagedObjectMetaDataImpl}.
	 * 
	 * @param configuration
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param officeFloorConfiguration
	 *            {@link OfficeFloorConfiguration} of the {@link OfficeFloor}
	 *            containing the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaDataImpl} or <code>null</code> if
	 *         issue.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> RawManagedObjectMetaData<D, F> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<F, MS> configuration,
			OfficeFloorIssues issues,
			OfficeFloorConfiguration officeFloorConfiguration);

}
