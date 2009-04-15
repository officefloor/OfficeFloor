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
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link OfficeFloorLoader} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorLoaderImpl implements OfficeFloorLoader {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	public OfficeFloorLoaderImpl(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
	}

	/*
	 * ======================= OfficeFloorLoader ===========================
	 */

	@Override
	public <OF extends OfficeFloorSource> PropertyList loadSpecification(
			Class<OF> officeFloorSourceClass, CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorLoader.loadSpecification");
	}

	@Override
	public <OF extends OfficeFloorSource> PropertyList init(
			Class<OF> officeFloorSourceClass,
			ConfigurationContext configurationContext,
			PropertyList propertyList, ClassLoader classLoader,
			CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorLoader.init");
	}

	@Override
	public <OF extends OfficeFloorSource> OfficeFloor loadOfficeFloor(
			Class<OF> officeFloorSourceClass,
			ConfigurationContext configurationContext,
			PropertyList propertyList, ClassLoader classLoader,
			CompilerIssues issues, OfficeFrame officeFrame) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorLoader.loadOfficeFloor");
	}

}