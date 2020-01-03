package net.officefloor.web.executive;

import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;

/**
 * {@link WebThreadAffinityExecutiveSource} {@link OfficeFloorExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityOfficeFloorExtensionService implements OfficeFloorExtensionService {

	/*
	 * ===================== OfficeFloorExtensionService =====================
	 */

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