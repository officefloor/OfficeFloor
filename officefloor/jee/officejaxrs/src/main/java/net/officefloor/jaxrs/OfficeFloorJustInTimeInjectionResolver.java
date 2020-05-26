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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link JustInTimeInjectionResolver}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJustInTimeInjectionResolver implements JustInTimeInjectionResolver {

	/**
	 * {@link OfficeFloorDependencies}.
	 */
	private final OfficeFloorDependencies dependencies;

	/**
	 * {@link ServiceLocator}.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * Instantiate.
	 * 
	 * @param dependencies   {@link OfficeFloorDependencies}.
	 * @param serviceLocator {@link ServiceLocator}.
	 */
	public OfficeFloorJustInTimeInjectionResolver(OfficeFloorDependencies dependencies, ServiceLocator serviceLocator) {
		this.dependencies = dependencies;
		this.serviceLocator = serviceLocator;
	}

	/*
	 * =================== JustInTimeInjectionResolver ===================
	 */

	@Override
	public boolean justInTimeResolution(Injectee failedInjectionPoint) {

		// Determine if able to supplied dependencies
		Object dependency = this.dependencies.getDependency(failedInjectionPoint.getParent());
		if (dependency == null) {
			return false; // did not find
		}

		// Bind in the object
		Class<?> requiredType = (Class<?>) failedInjectionPoint.getRequiredType();
		Set<Type> contracts = new HashSet<Type>(Arrays.asList(requiredType));
		Set<Annotation> qualifiers = new HashSet<Annotation>(failedInjectionPoint.getRequiredQualifiers());
		OfficeFloorHk2Object<Object> officeFloorHk2Object = new OfficeFloorHk2Object<Object>(requiredType.getName(),
				contracts, qualifiers, requiredType, dependency);
		ServiceLocatorUtilities.addOneDescriptor(this.serviceLocator, officeFloorHk2Object, false);

		// Successfully made available
		return true;
	}

}
