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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;

import jakarta.inject.Singleton;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link OfficeFloor} {@link ActiveDescriptor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHk2Object<T> extends AbstractActiveDescriptor<T> {

	/**
	 * Type of object.
	 */
	private Class<?> type;

	/**
	 * {@link ManagedObject} object from {@link OfficeFloor}.
	 */
	private T object;

	/**
	 * Allows serialisation.
	 */
	public OfficeFloorHk2Object() {
		// allow serialisation
	}

	/**
	 * Instantiate for binding in {@link ManagedObject} from {@link OfficeFloor}.
	 * 
	 * @param name       Name of {@link ManagedObject}.
	 * @param contracts  Contracts.
	 * @param qualifiers Qualifiers.
	 * @param type       Type of object from {@link ManagedObject}.
	 * @param object     {@link ManagedObject} object.
	 */
	public OfficeFloorHk2Object(String name, Set<Type> contracts, Set<Annotation> qualifiers, Class<?> type, T object) {
		super(contracts, Singleton.class, name, qualifiers, DescriptorType.CLASS, DescriptorVisibility.NORMAL, 0, false,
				null, (String) null, new HashMap<String, List<String>>());
		super.setImplementation(type.getName());
		this.type = type;
		this.object = object;
	}

	/*
	 * ================= ActiveDescriptor ===================
	 */

	@Override
	public Class<?> getImplementationClass() {
		return this.type;
	}

	@Override
	public Type getImplementationType() {
		return this.type;
	}

	@Override
	public T create(ServiceHandle<?> root) {
		return this.object;
	}

}
