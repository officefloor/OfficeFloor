package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.executive.TeamOversightType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.executive.Executive;
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
	 * Obtains the {@link OfficeFloorTeamOversight} for {@link TeamOversightType}.
	 * 
	 * @param teamOversightName Name of {@link TeamOversightType}.
	 * @return {@link OfficeFloorTeamOversight}.
	 */
	OfficeFloorTeamOversight getOfficeFloorTeamOversight(String teamOversightName);

}