/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.office;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.Office;

/**
 * Ensure able to override the {@link PropertyList} for various aspects of the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeOverridePropertiesTest extends AbstractCompileTestCase {

	/**
	 * Ensure can override {@link Property} for the {@link OfficeSection}.
	 */
	public void testOverrideSectionProperty() {
		fail("TODO implement");
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeGovernance}.
	 */
	public void testOverrideGovernanceProperty() {
		fail("TODO implement");
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link OfficeAdministration}.
	 */
	public void testOverrideAdministrationProperty() {
		fail("TODO implement");
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link OfficeManagedObjectSource}.
	 */
	public void testOverrideManagedObjectSourceProperty() {
		fail("TODO implement");
	}

}