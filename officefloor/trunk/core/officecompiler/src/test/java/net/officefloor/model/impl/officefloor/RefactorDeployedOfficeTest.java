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