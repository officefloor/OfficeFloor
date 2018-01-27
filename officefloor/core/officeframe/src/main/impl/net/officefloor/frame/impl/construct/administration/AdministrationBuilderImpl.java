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
package net.officefloor.frame.impl.construct.administration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.construct.function.AbstractFunctionBuilder;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;

/**
 * Implementation of the {@link AdministrationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationBuilderImpl<E, F extends Enum<F>, G extends Enum<G>> extends AbstractFunctionBuilder<F>
		implements AdministrationBuilder<F, G>, AdministrationConfiguration<E, F, G> {

	/**
	 * Name of this {@link Administration}.
	 */
	private final String administrationName;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<E, F, G> administrationFactory;

	/**
	 * Listing of the scope names of the {@link ManagedObject} instances.
	 */
	private final List<String> administeredManagedObjectNames = new LinkedList<String>();

	/**
	 * Registry of {@link Governance} instances that may be invoked from the
	 * {@link AdministrationDuty}.
	 */
	private final Map<Integer, AdministrationGovernanceConfiguration<?>> governances = new HashMap<Integer, AdministrationGovernanceConfiguration<?>>();

	/**
	 * Initiate.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 */
	public AdministrationBuilderImpl(String administrationName, Class<E> extensionInterface,
			AdministrationFactory<E, F, G> administrationFactory) {
		this.administrationName = administrationName;
		this.extensionInterface = extensionInterface;
		this.administrationFactory = administrationFactory;
	}

	/*
	 * ================ AdministratorBuilder ==============================
	 */

	@Override
	public void administerManagedObject(String scopeManagedObjectName) {
		this.administeredManagedObjectNames.add(scopeManagedObjectName);
	}

	@Override
	public void linkGovernance(G key, String governanceName) {
		this.linkGovernance(key, key.ordinal(), governanceName);
	}

	@Override
	public void linkGovernance(int governanceIndex, String governanceName) {
		this.linkGovernance(null, governanceIndex, governanceName);
	}

	/**
	 * Links the {@link Governance}.
	 * 
	 * @param key
	 *            Key to access the {@link Governance} from the
	 *            {@link AdministrationDuty}.
	 * @param governanceIndex
	 *            Index of the {@link Governance} from the
	 *            {@link AdministrationDuty}.
	 * @param governanceName
	 *            Name of the {@link Governance} to link.
	 */
	private void linkGovernance(G key, int governanceIndex, String governanceName) {
		this.governances.put(Integer.valueOf(governanceIndex),
				new AdministrationGovernanceConfigurationImpl<G>(key, governanceIndex, governanceName));
	}

	/*
	 * ============= AdministrationConfiguration =====================
	 */

	@Override
	public String getAdministrationName() {
		return this.administrationName;
	}

	@Override
	public AdministrationFactory<E, F, G> getAdministrationFactory() {
		return this.administrationFactory;
	}

	@Override
	public Class<E> getExtensionType() {
		return this.extensionInterface;
	}

	@Override
	public AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration() {

		// Obtain the array size from maximum index
		int arraySize = -1;
		for (Integer key : this.governances.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one up of max index

		// Create the listing of governance
		AdministrationGovernanceConfiguration<?>[] governanceList = new AdministrationGovernanceConfiguration[arraySize];
		for (Integer key : this.governances.keySet()) {
			AdministrationGovernanceConfiguration<?> governance = this.governances.get(key);
			governanceList[key.intValue()] = governance;
		}

		// Return the listing
		return governanceList;
	}

	@Override
	public String[] getAdministeredManagedObjectNames() {
		return this.administeredManagedObjectNames.toArray(new String[0]);
	}

}