package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;

/**
 * {@link AdministrationParameterFactory} to obtain the
 * {@link GovernanceManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationGovernanceParameterFactory implements AdministrationParameterFactory {

	/**
	 * Index of the {@link GovernanceManager}.
	 */
	private final int governanceIndex;

	/**
	 * Initiate.
	 * 
	 * @param governanceIndex
	 *            Index of the {@link GovernanceManager}.
	 */
	public AdministrationGovernanceParameterFactory(int governanceIndex) {
		this.governanceIndex = governanceIndex;
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception {
		return context.getGovernance(this.governanceIndex);
	}

}