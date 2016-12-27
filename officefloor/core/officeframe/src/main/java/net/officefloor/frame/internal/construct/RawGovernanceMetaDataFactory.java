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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * Factory for the creation of the {@link RawGovernanceMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawGovernanceMetaDataFactory {

	/**
	 * Creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            Flow key type.
	 * @param configuration
	 *            {@link GovernanceConfiguration}.
	 * @param governanceIndex
	 *            Index of the {@link Governance} within the
	 *            {@link ProcessState}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office} name.
	 * @param officeName
	 *            Name of the {@link Office} having {@link Governance} added.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	<E, F extends Enum<F>> RawGovernanceMetaData<E, F> createRawGovernanceMetaData(
			GovernanceConfiguration<E, F> configuration, int governanceIndex, SourceContext sourceContext,
			Map<String, TeamManagement> officeTeams, String officeName, OfficeFloorIssues issues,
			FunctionLoop functionLoop);

}