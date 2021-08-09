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

package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Sources the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
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
	void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception;

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
	void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception;

}
