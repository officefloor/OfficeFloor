/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.office;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;

/**
 * Tests refactoring the {@link OfficeSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeSectionTest extends
		AbstractRefactorOfficeSectionTest {

	/**
	 * Tests renaming the {@link OfficeSectionModel}.
	 */
	public void testRenameOfficeSection() {
		this.refactor_officeSectionName("NEW_NAME");
		this.doRefactor();
	}

	/**
	 * Ensure can change {@link SectionSource} class name.
	 */
	public void testChangeSectionSource() {
		this.refactor_sectionSourceClassName("net.another.AnotherSectionSource");
		this.doRefactor();
	}

	/**
	 * Ensure change location of {@link OfficeSectionModel}.
	 */
	public void testChangeSectionLocation() {
		this.refactor_sectionLocation("ANOTHER_LOCATION");
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
	 * Ensure can refactor the {@link OfficeSectionInputModel} instances.
	 */
	public void testRefactorInputs() {
		this.refactor_mapInput("CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapInput("RENAME_NEW", "RENAME_OLD");
		this.doRefactor(new OfficeSectionConstructor() {
			@Override
			public void construct(OfficeSectionContext context) {
				context.addOfficeSectionInput("CHANGE_DETAILS", Integer.class);
				context.addOfficeSectionInput("RENAME_NEW", Object.class);
			}
		});
	}

	/**
	 * Ensure can refactor the {@link OfficeSectionOutputModel} instances.
	 */
	public void testRefactorOutputs() {
		this.refactor_mapOutput("CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapOutput("RENAME_NEW", "RENAME_OLD");
		this.doRefactor(new OfficeSectionConstructor() {
			@Override
			public void construct(OfficeSectionContext context) {
				context.addOfficeSectionOutput("CHANGE_DETAILS", Integer.class,
						false);
				context.addOfficeSectionOutput("RENAME_NEW", Object.class, true);
			}
		});
	}

	/**
	 * Ensure can refactor the {@link OfficeSectionObjectModel} instances.
	 */
	public void testRefactorObjects() {
		this.refactor_mapObject("CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapObject("RENAME_NEW", "RENAME_OLD");
		this.doRefactor(new OfficeSectionConstructor() {
			@Override
			public void construct(OfficeSectionContext context) {
				context.addOfficeSectionObject("CHANGE_DETAILS", Integer.class,
						null);
				context.addOfficeSectionObject("RENAME_NEW", Object.class, null);
			}
		});
	}

	/**
	 * Ensures removes necessary connections in refactoring.
	 */
	public void testRemoveConnections() {
		this.doRefactor();
	}

}