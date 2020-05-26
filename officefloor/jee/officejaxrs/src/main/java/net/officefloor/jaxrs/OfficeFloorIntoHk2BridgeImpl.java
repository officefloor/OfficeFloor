/*-
 * #%L
 * JAX-RS
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jaxrs;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * {@link OfficeFloorIntoHk2Bridge} implementation.
 * 
 * @author Daniel Sagenschneider
 */
@Singleton
public class OfficeFloorIntoHk2BridgeImpl implements OfficeFloorIntoHk2Bridge {

	/**
	 * {@link ServiceLocator}.
	 */
	private @Inject ServiceLocator serviceLocator;

	/*
	 * ================= OfficeFloorIntoHk2Bridge ===================
	 */

	@Override
	public void bridgeOfficeFloor(OfficeFloorDependencies dependencies) {
		OfficeFloorJustInTimeInjectionResolver justInTimeResolver = new OfficeFloorJustInTimeInjectionResolver(
				dependencies, this.serviceLocator);
		ServiceLocatorUtilities.addOneConstant(this.serviceLocator, justInTimeResolver);
	}

}
