/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Deployed {@link Office} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeployedOffice extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link DeployedOffice}.
	 * 
	 * @return Name of this {@link DeployedOffice}.
	 */
	String getDeployedOfficeName();

	/**
	 * <p>
	 * Adds an additional profile specific to the {@link DeployedOffice}.
	 * <p>
	 * All {@link Node} instances within this {@link DeployedOffice} will have this
	 * additional profile.
	 * 
	 * @param profile Profile specific to the {@link DeployedOffice}.
	 */
	void addAdditionalProfile(String profile);

	/**
	 * <p>
	 * Adds an override {@link Property}.
	 * <p>
	 * This allows overriding configuration of the {@link DeployedOffice}.
	 * {@link Property} instances match on qualified name of the {@link Node}. The
	 * remainder of the name is the {@link Property} name being overridden for the
	 * {@link Node}.
	 * 
	 * @param name  Name of {@link Property}.
	 * @param value Value for {@link Property}.
	 */
	void addOverrideProperty(String name, String value);

	/**
	 * Obtains the {@link DeployedOfficeInput} for the
	 * {@link OfficeAvailableSectionInputType}.
	 * 
	 * @param sectionName Name of the {@link OfficeSection} providing the
	 *                    {@link OfficeAvailableSectionInputType}.
	 * @param inputName   Name of the {@link OfficeAvailableSectionInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String sectionName, String inputName);

	/**
	 * Obtains the {@link OfficeTeam} for the {@link OfficeTeamType}.
	 * 
	 * @param officeTeamName Name of the {@link OfficeTeamType}.
	 * @return {@link OfficeTeam}.
	 */
	OfficeTeam getDeployedOfficeTeam(String officeTeamName);

	/**
	 * Obtains the {@link OfficeObject} for the {@link OfficeManagedObjectType}.
	 * 
	 * @param officeManagedObjectName Name of the {@link OfficeManagedObjectType}.
	 * @return {@link OfficeObject}.
	 */
	OfficeObject getDeployedOfficeObject(String officeManagedObjectName);

}
