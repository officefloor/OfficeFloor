/*-
 * #%L
 * Web Executive
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

package net.officefloor.web.executive;

import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link WebThreadAffinityExecutiveSource} {@link OfficeFloorExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/*
	 * ===================== OfficeFloorExtensionService =====================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Create the Executive
		OfficeFloorExecutive executive = officeFloorDeployer
				.setExecutive(WebThreadAffinityExecutiveSource.class.getName());

		// Configure thread affinity for team oversights
		OfficeFloorTeamOversight oversight = executive.getOfficeFloorTeamOversight("CORE_AFFINITY");
		officeFloorDeployer.addTeamAugmentor((teamAugment) -> teamAugment.setTeamOversight(oversight));

		// Configure thread affinity for execution strategies
		OfficeFloorExecutionStrategy executionStrategy = executive.getOfficeFloorExecutionStrategy("CPU_AFFINITY");
		officeFloorDeployer.addManagedObjectSourceAugmentor((mos) -> {
			for (ManagedObjectExecutionStrategyType executionStrategyType : mos.getManagedObjectType()
					.getExecutionStrategyTypes()) {
				mos.link(mos.getManagedObjectExecutionStrategy(executionStrategyType.getExecutionStrategyName()),
						executionStrategy);
			}
		});
	}

}