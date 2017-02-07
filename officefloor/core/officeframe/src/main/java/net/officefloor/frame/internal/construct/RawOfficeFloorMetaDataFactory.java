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
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaDataImpl;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Factory for creating {@link RawOfficeFloorMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawOfficeFloorMetaDataFactory {

	/**
	 * Constructs the {@link RawOfficeFloorMetaDataImpl} from the
	 * {@link OfficeFloorConfiguration}.
	 * 
	 * @param configuration
	 *            {@link OfficeFloorConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param rawTeamFactory
	 *            {@link RawTeamMetaDataFactory}.
	 * @param threadLocalAwareExecutor
	 *            {@link ThreadLocalAwareExecutor}.
	 * @param rawMosFactory
	 *            {@link RawManagedObjectMetaDataFactory}.
	 * @param rawBoundMoFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawGovernanceFactory
	 *            {@link RawGovernanceMetaDataFactory}.
	 * @param rawBoundAdminFactory
	 *            {@link RawAdministrationMetaDataFactory}.
	 * @param rawOfficeFactory
	 *            {@link RawOfficeMetaDataFactory}.
	 * @param rawFunctionFactory
	 *            {@link RawManagedFunctionMetaDataFactory}.
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	RawOfficeFloorMetaData constructRawOfficeFloorMetaData(OfficeFloorConfiguration configuration,
			OfficeFloorIssues issues, RawTeamMetaDataFactory rawTeamFactory,
			ThreadLocalAwareExecutor threadLocalAwareExecutor, RawManagedObjectMetaDataFactory rawMosFactory,
			RawBoundManagedObjectMetaDataFactory rawBoundMoFactory, RawGovernanceMetaDataFactory rawGovernanceFactory,
			RawAdministrationMetaDataFactory rawBoundAdminFactory, RawOfficeMetaDataFactory rawOfficeFactory,
			RawManagedFunctionMetaDataFactory rawFunctionFactory);

}