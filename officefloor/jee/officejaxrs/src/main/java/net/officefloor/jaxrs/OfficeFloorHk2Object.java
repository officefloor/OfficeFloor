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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;

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
