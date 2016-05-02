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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeSectionInputType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Node within the hierarchy of {@link OfficeSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionNode extends SectionDesigner, SectionType, SubSection,
		OfficeSection {

	/**
	 * Indicates if this {@link OfficeSection} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Initialises this {@link OfficeSection}.
	 * 
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	void initialise(String sectionSourceClassName, String sectionLocation,
			PropertyList properties);

	/**
	 * Initialises this {@link OfficeSection}.
	 * 
	 * @param sectionSource
	 *            {@link SectionSource} instance.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	void initialise(SectionSource sectionSource, String sectionLocation,
			PropertyList properties);

	/**
	 * Obtains the {@link OfficeSectionInputType} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionInputType} instances for this {@link OfficeSection}.
	 */
	OfficeSectionInputType[] getOfficeInputTypes();

	/**
	 * Obtains the {@link DeployedOfficeInput}.
	 * 
	 * @param inputName
	 *            Input name as per the {@link OfficeSectionInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String inputName);

	/**
	 * <p>
	 * Obtains the {@link GovernanceNode} instances providing {@link Governance}
	 * over this {@link SectionNode}.
	 * <p>
	 * This is list comprised of the:
	 * <ol>
	 * <li>{@link GovernanceNode} assigned to this particular
	 * {@link SectionNode}</li>
	 * <li>{@link GovernanceNode} assigned to any parent {@link SectionNode}</li>
	 * </ol>
	 * 
	 * @return {@link GovernanceNode} instances providing {@link Governance}
	 *         over this {@link SectionNode}.
	 */
	GovernanceNode[] getGoverningGovernances();

	/**
	 * Loads the {@link OfficeSection} of this {@link SectionNode} and all its
	 * {@link SubSection} {@link SectionNode} instances.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeSection}.
	 */
	void loadOfficeSection(String officeLocation);

	/**
	 * Obtains the {@link OfficeNode} containing this {@link SectionNode}.
	 * 
	 * @return {@link OfficeNode} containing this {@link SectionNode}.
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtains the parent {@link SectionNode} containing this
	 * {@link SectionNode}.
	 * 
	 * @return Parent {@link SectionNode} or <code>null</code> if this
	 *         {@link SectionNode} is the top level {@link SectionNode} (in
	 *         other words a {@link OfficeSection}).
	 */
	SectionNode getParentSectionNode();

	/**
	 * Obtain the {@link OfficeSection} qualified name.
	 * 
	 * @param simpleName
	 *            Simple name to qualify with the {@link OfficeSection} name
	 *            space.
	 * @return {@link OfficeSection} qualified name.
	 */
	String getSectionQualifiedName(String simpleName);

	/**
	 * Builds this {@link OfficeSection} for this {@link SectionNode}.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @param officeNode
	 *            {@link OfficeNode} containing this {@link SectionNode}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} of the {@link Office} containing this
	 *            {@link SectionNode}.
	 */
	void buildSection(OfficeFloorBuilder officeFloorBuilder,
			OfficeNode officeNode, OfficeBuilder officeBuilder);

}