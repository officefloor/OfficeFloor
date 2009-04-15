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
package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Sources the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorSource {

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorSourceSpecification} for this
	 * {@link OfficeFloorSource}.
	 * <p>
	 * This enables the {@link OfficeFloorSourceContext} to be populated with
	 * the necessary details as per this {@link OfficeFloorSourceSpecification}
	 * in deploying the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorSourceSpecification}.
	 */
	OfficeFloorSourceSpecification getSpecification();

	/**
	 * <p>
	 * Obtains from the {@link OfficeFloorSource} any additional
	 * {@link Property} instances required to source the {@link OfficeFloor}
	 * after interrogating the configuration.
	 * <p>
	 * This method is a separate initial step from the
	 * {@link #sourceOfficeFloor(OfficeFloorDeployer, OfficeFloorSourceContext)}
	 * method to enable specifying any required {@link Property} instances once
	 * the necessary {@link ConfigurationItem} instances have been interrogated.
	 * <p>
	 * Typically this allows environment specific properties to be defined
	 * externally so that deployment configuration need not be repeated per
	 * environment. In other words, one set of deployment configuration with
	 * properties providing the differences between the environments.
	 * <p>
	 * This also enables sensitive properties, such as <code>passwords</code>,
	 * to not be contained in deployment configuration but within a
	 * &quot;secure&quot; location.
	 * 
	 * @param requiredProperties
	 *            Populated by the {@link OfficeFloorSource} with any additional
	 *            {@link Property} instances required to source the
	 *            {@link OfficeFloor}.
	 * @param context
	 *            {@link OfficeFloorSourceContext} populated with
	 *            {@link Property} instances as per the
	 *            {@link OfficeFloorSourceSpecification}.
	 * @throws Exception
	 *             If fails to initialise the {@link OfficeFloorSource}.
	 */
	void specifyConfigurationProperties(RequiredProperties requiredProperties,
			OfficeFloorSourceContext context) throws Exception;

	/**
	 * Sources the {@link OfficeFloor} by deploying it via the input
	 * {@link OfficeFloorDeployer}.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer} to deploy the {@link OfficeFloor}.
	 * @param context
	 *            {@link OfficeFloorSourceContext} populated with the
	 *            {@link Property} instances as per the
	 *            {@link OfficeFloorSourceSpecification} and
	 *            {@link RequiredProperties}. Should there be a name clash
	 *            between the two, the {@link RequiredProperties}
	 *            {@link Property} will be used.
	 * @throws Exception
	 *             If fails to source the {@link OfficeFloor}.
	 */
	void sourceOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception;

}