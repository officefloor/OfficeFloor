/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Executive} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorExecutive extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorExecutive}.
	 * 
	 * @return Name of this {@link OfficeFloorExecutive}.
	 */
	String getOfficeFloorExecutiveName();

	/**
	 * Obtains the {@link OfficeFloorExecutionStrategy} for
	 * {@link ExecutionStrategyType}.
	 * 
	 * @param executionStrategyName Name of {@link ExecutionStrategyType}.
	 * @return {@link OfficeFloorExecutionStrategy}.
	 */
	OfficeFloorExecutionStrategy getOfficeFloorExecutionStrategy(String executionStrategyName);

	/**
	 * Request to have no {@link TeamOversight}.
	 */
	void requestNoTeamOversight();

}
