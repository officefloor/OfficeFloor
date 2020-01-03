package net.officefloor.frame.impl.construct.executive;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * Raw {@link Executive} meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaData {

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * {@link Map} of {@link ExecutionStrategy} name to its {@link ThreadFactory}
	 * instances.
	 */
	private final Map<String, ThreadFactory[]> executionStrategies;

	/**
	 * {@link TeamOversight} instances by their names.
	 */
	private final Map<String, TeamOversight> teamOversights;

	/**
	 * Instantiate.
	 * 
	 * @param executive           {@link Exception}.
	 * @param executionStrategies {@link Map} of {@link ExecutionStrategy} name to
	 *                            its {@link ThreadFactory} instances.
	 * @param teamOversights      {@link TeamOversight} instances by their names.
	 */
	public RawExecutiveMetaData(Executive executive, Map<String, ThreadFactory[]> executionStrategies,
			Map<String, TeamOversight> teamOversights) {
		this.executive = executive;
		this.executionStrategies = executionStrategies;
		this.teamOversights = teamOversights;
	}

	/**
	 * Obtains the {@link Executive}.
	 * 
	 * @return {@link Executive}.
	 */
	public Executive getExecutive() {
		return this.executive;
	}

	/**
	 * Obtains the {@link ExecutionStrategy} instances by their names.
	 * 
	 * @return {@link ExecutionStrategy} instances by their names.
	 */
	public Map<String, ThreadFactory[]> getExecutionStrategies() {
		return this.executionStrategies;
	}

	/**
	 * Obtains the {@link TeamOversight} instances by their name.
	 * 
	 * @return {@link TeamOversight} instances by their name.
	 */
	public Map<String, TeamOversight> getTeamOversights() {
		return this.teamOversights;
	}

}