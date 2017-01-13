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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;

/**
 * Factory for the creation of {@link RawManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagedObjectMetaDataFactory {

	/**
	 * Creates the {@link RawManagedObjectMetaDataImpl}.
	 * 
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param configuration
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param officeFloorConfiguration
	 *            {@link OfficeFloorConfiguration} of the {@link OfficeFloor}
	 *            containing the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaDataImpl} or <code>null</code> if
	 *         issue.
	 */
	<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> RawManagedObjectMetaData<O, F> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<F, MS> configuration, SourceContext sourceContext,
			OfficeFloorIssues issues, OfficeFloorConfiguration officeFloorConfiguration);

}