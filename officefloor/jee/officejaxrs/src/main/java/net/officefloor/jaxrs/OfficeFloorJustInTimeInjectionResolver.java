/*-
 * #%L
 * JAX-RS
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
