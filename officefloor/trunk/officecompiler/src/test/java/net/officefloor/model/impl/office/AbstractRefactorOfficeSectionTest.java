/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.PropertyModel;

/**
 * Abstract functionality for refactoring {@link OfficeSectionModel} to an
 * {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRefactorOfficeSectionTest extends
		AbstractOfficeChangesTestCase {

	/**
	 * {@link OfficeSectionModel} to refactor.
	 */
	private OfficeSectionModel sectionModel;

	/**
	 * Name to refactor the {@link OfficeSectionModel} to have.
	 */
	private String sectionName;

	/**
	 * {@link SectionSource} class name to refactor the
	 * {@link OfficeSectionModel} to have.
	 */
	private String sectionSourceClassName;

	/**
	 * Location to refactor the {@link OfficeSectionModel} to have.
	 */
	private String sectionLocation;

	/**
	 * {@link PropertyList} to refactor the {@link OfficeSectionModel} to have.
	 */
	private PropertyList properties = null;

	/**
	 * Mapping of {@link OfficeSectionInput} name to
	 * {@link OfficeSectionInputModel} name.
	 */
	private final Map<String, String> inputNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeSectionOutput} name to
	 * {@link OfficeSectionOutputModel} name.
	 */
	private final Map<String, String> outputNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeSectionObject} name to
	 * {@link OfficeSectionObjectModel} name.
	 */
	private final Map<String, String> objectNameMapping = new HashMap<String, String>();

	/**
	 * Initiate for specific setup per test.
	 */
	public AbstractRefactorOfficeSectionTest() {
		super(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.officefloor.model.impl.AbstractChangesTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Setup the model
		super.setUp();

		// Obtain office section model and specify details from it
		this.sectionModel = this.model.getOfficeSections().get(0);
		this.sectionName = this.sectionModel.getOfficeSectionName();
		this.sectionSourceClassName = this.sectionModel
				.getSectionSourceClassName();
		this.sectionLocation = this.sectionModel.getSectionLocation();
	}

	/**
	 * Refactors the {@link OfficeSectionModel} name.
	 *
	 * @param newName
	 *            New name for the {@link OfficeSectionModel}.
	 */
	protected void refactor_officeSectionName(String newName) {
		this.sectionName = newName;
	}

	/**
	 * Convenience method to do refactoring with a simple {@link OfficeSection}.
	 */
	protected void doRefactor() {
		this.doRefactor((OfficeSection) null);
	}

	/**
	 * Does the refactoring and validates applying and reverting.
	 *
	 * @param officeSection
	 *            {@link OfficeSection}.
	 */
	protected void doRefactor(OfficeSection officeSection) {

		// Ensure have office section
		if (officeSection == null) {
			// Provide simple office section
			officeSection = this
					.constructOfficeSection(new OfficeSectionConstructor() {
						@Override
						public void construct(OfficeSectionContext context) {
							// Keep simple
						}
					});
		}

		// Create the property list
		PropertyList propertyList = this.properties;
		if (propertyList == null) {
			// Not refactoring properties, so take from work model
			propertyList = new PropertyListImpl();
			for (PropertyModel property : this.sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}
		}

		// Create the change to refactory the office section
		Change<OfficeSectionModel> change = this.operations
				.refactorOfficeSection(this.sectionModel, this.sectionName,
						this.sectionSourceClassName, this.sectionLocation,
						propertyList, officeSection, this.inputNameMapping,
						this.outputNameMapping, this.objectNameMapping);

		// Assert the refactoring changes
		this.assertChange(change, this.sectionModel, "Refactor office section",
				true);
	}
}