/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.governance;

import java.util.Map;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory for the creation of the {@link RawGovernanceMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataFactory {

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link Office} {@link TeamManagement} instances by their name.
	 */
	private final Map<String, TeamManagement> officeTeams;

	/**
	 * Instantiate.
	 * 
	 * @param officeName  Name of the {@link Office} having {@link Governance}
	 *                    added.
	 * @param officeTeams {@link TeamManagement} instances by their {@link Office}
	 *                    name.
	 */
	public RawGovernanceMetaDataFactory(String officeName, Map<String, TeamManagement> officeTeams) {
		this.officeName = officeName;
		this.officeTeams = officeTeams;
	}

	/**
	 * Creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param <E>                            Extension interface type.
	 * @param <F>                            Flow key type.
	 * @param configuration                  {@link GovernanceConfiguration}.
	 * @param governanceIndex                Index of the {@link Governance} within
	 *                                       the {@link ProcessState}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	public <E, F extends Enum<F>> RawGovernanceMetaData<E, F> createRawGovernanceMetaData(
			GovernanceConfiguration<E, F> configuration, int governanceIndex, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the governance name
		String governanceName = configuration.getGovernanceName();
		if (ConstructUtil.isBlank(governanceName)) {
			issues.addIssue(AssetType.OFFICE, this.officeName, "Governance added without a name");
			return null; // can not carry on
		}

		// Obtain the governance factory
		GovernanceFactory<? super E, F> governanceFactory = configuration.getGovernanceFactory();
		if (governanceFactory == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No " + GovernanceFactory.class.getSimpleName() + " provided for governance " + governanceName);
			return null; // can not carry on
		}

		// Obtain the extension interface type
		Class<E> extensionInterfaceType = configuration.getExtensionType();
		if (extensionInterfaceType == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No extension type provided for governance " + governanceName);
			return null; // can not carry on
		}

		// Obtain the team responsible for governance
		TeamManagement responsibleTeam = null;
		String teamName = configuration.getResponsibleTeamName();
		if (!ConstructUtil.isBlank(teamName)) {
			responsibleTeam = this.officeTeams.get(teamName);
			if (responsibleTeam == null) {
				issues.addIssue(AssetType.GOVERNANCE, governanceName, "Can not find " + Team.class.getSimpleName()
						+ " by name '" + teamName + "' for governance " + governanceName);
				return null; // can not carry on
			}
		}

		// Obtain the asynchronous flow timeout
		long asynchronousFlowTimeout = configuration.getAsynchronousFlowTimeout();
		if (asynchronousFlowTimeout <= 0) {
			asynchronousFlowTimeout = defaultAsynchronousFlowTimeout;
		}

		// Create the asynchronous flow asset manager
		AssetManagerReference asynchronousFlowAssetManagerReference = assetManagerRegistry.createAssetManager(
				AssetType.GOVERNANCE, governanceName, AsynchronousFlow.class.getSimpleName(), issues);

		// Create the logger
		Logger logger = OfficeFrame.getLogger(governanceName);

		// Create the Governance Meta-Data
		GovernanceMetaDataImpl<E, F> governanceMetaData = new GovernanceMetaDataImpl<>(governanceName,
				governanceFactory, responsibleTeam, asynchronousFlowTimeout, asynchronousFlowAssetManagerReference,
				logger);

		// Create the raw Governance meta-data
		RawGovernanceMetaData<E, F> rawGovernanceMetaData = new RawGovernanceMetaData<>(governanceName, governanceIndex,
				extensionInterfaceType, configuration, governanceMetaData);

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

}
