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

package net.officefloor.model.impl.officefloor;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;

/**
 * Tests refactoring the {@link DeployedOfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorDeployedOfficeTest extends
		AbstractRefactorDeployedOfficeTest {

	/**
	 * Renames the {@link DeployedOfficeModel}.
	 */
	public void testRenameDeployedOffice() {
		this.refactor_deployedOfficeName("NEW_NAME");
		this.doRefactor();
	}

	/**
	 * Ensure can change {@link OfficeSource} class name.
	 */
	public void testChangeOfficeSource() {
		this.refactor_officeSourceClassName("net.another.AnotherOfficeSource");
		this.doRefactor();
	}

	/**
	 * Ensure can change {@link DeployedOfficeModel} location.
	 */
	public void testChangeOfficeLocation() {
		this.refactor_officeLocation("ANOTHER_LOCATION");
		this.doRefactor();
	}

	/**
	 * Ensure can change {@link PropertyList}.
	 */
	public void testChangeProperties() {
		this.refactor_addProperty("ANOTHER_NAME", "ANOTHER_VALUE");
		this.doRefactor();
	}

	/**
	 * Ensure can refactor the {@link DeployedOfficeInputModel} instances.
	 */
	public void testRefactorInputs() {
		this.refactor_mapInput("CHANGE:DETAILS", "CHANGE:DETAILS");
		this.refactor_mapInput("RENAME:NEW", "RENAME:OLD");
		this.doRefactor(new OfficeTypeConstructor() {
			@Override
			public void construct(OfficeTypeContext context) {
				context.addOfficeSectionInput("CHANGE", "DETAILS",
						Integer.class);
				context.addOfficeSectionInput("RENAME", "NEW", Object.class);
			}
		});
	}

	/**
	 * Ensure can refactor the {@link DeployedOfficeObjectModel} instances.
	 */
	public void testRefactorObjects() {
		this.refactor_mapObject("CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapObject("RENAME_NEW", "RENAME_OLD");
		this.doRefactor(new OfficeTypeConstructor() {
			@Override
			public void construct(OfficeTypeContext context) {
				context.addOfficeManagedObject("CHANGE_DETAILS", Integer.class,
						null, XAResource.class);
				context.addOfficeManagedObject("RENAME_NEW", Object.class, null);
			}
		});
	}

	/**
	 * Ensure can refactor the {@link DeployedOfficeTeamModel} instances.
	 */
	public void testRefactorTeams() {
		this.refactor_mapTeam("RENAME_NEW", "RENAME_OLD");
		this.doRefactor(new OfficeTypeConstructor() {
			@Override
			public void construct(OfficeTypeContext context) {
				context.addOfficeTeam("RENAME_NEW");
			}
		});
	}

	/**
	 * Ensure removes necessary connections on refactoring.
	 */
	public void testRemoveConnections() {
		this.doRefactor();
	}

}
