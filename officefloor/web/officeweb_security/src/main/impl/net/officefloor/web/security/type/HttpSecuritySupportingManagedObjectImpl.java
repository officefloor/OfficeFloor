/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * {@link HttpSecuritySupportingManagedObject} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySupportingManagedObjectImpl<O extends Enum<O>>
		implements HttpSecuritySupportingManagedObject<O>, HttpSecuritySupportingManagedObjectType<O> {

	/**
	 * Name of the {@link HttpSecuritySupportingManagedObject}.
	 */
	private final String name;

	/**
	 * {@link ManagedObjectSource} for the
	 * {@link HttpSecuritySupportingManagedObject}.
	 */
	private final ManagedObjectSource<O, ?> managedObjectSource;

	/**
	 * {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link ManagedObjectScope} for the {@link ManagedObject}.
	 */
	private final ManagedObjectScope managedObjectScope;

	/**
	 * {@link HttpSecuritySupportingManagedObjectDependencyType} instances.
	 */
	private final List<HttpSecuritySupportingManagedObjectDependencyType<O>> dependencies = new LinkedList<>();

	/**
	 * Object type.
	 */
	private Class<?> objectType = null;

	/**
	 * Instantiate.
	 * 
	 * @param name                Name of the
	 *                            {@link HttpSecuritySupportingManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource} for the
	 *                            {@link HttpSecuritySupportingManagedObject}.
	 * @param propertyListFactory Factory to create a {@link PropertyList}.
	 * @param managedObjectScope  {@link ManagedObjectScope} for the
	 *                            {@link ManagedObject}.
	 */
	public HttpSecuritySupportingManagedObjectImpl(String name, ManagedObjectSource<O, ?> managedObjectSource,
			Supplier<PropertyList> propertyListFactory, ManagedObjectScope managedObjectScope) {
		this.name = name;
		this.managedObjectSource = managedObjectSource;
		this.propertyList = propertyListFactory.get();
		this.managedObjectScope = managedObjectScope;
	}

	/**
	 * Loads the {@link HttpSecuritySupportingManagedObjectType}.
	 * 
	 * @param managedObjectTypeLoader Loader to load the {@link ManagedObjectType}.
	 * @return {@link HttpSecuritySupportingManagedObjectType}.
	 */
	public HttpSecuritySupportingManagedObjectType<?> loadHttpSecuritySupportingManagedObjectType(
			BiFunction<ManagedObjectSource<?, ?>, PropertyList, ManagedObjectType<?>> managedObjectTypeLoader) {

		// Load the managed object type
		ManagedObjectType<?> managedObjectType = managedObjectTypeLoader.apply(this.managedObjectSource,
				this.propertyList);
		if (managedObjectType == null) {
			return null; // failed to load type
		}

		// Load the object type
		this.objectType = managedObjectType.getObjectType();

		// Return the supporting managed object type
		return this;
	}

	/*
	 * =============== HttpSecuritySupportingManagedObject ==============
	 */

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public void linkAuthentication(O dependency) {
		this.link(dependency, (context) -> context.getAuthentication());
	}

	@Override
	public void linkHttpAuthentication(O dependency) {
		this.link(dependency, (context) -> context.getHttpAuthentication());
	}

	@Override
	public void linkAccessControl(O dependency) {
		this.link(dependency, (context) -> context.getAccessControl());
	}

	@Override
	public void linkHttpAccessControl(O dependency) {
		this.link(dependency, (context) -> context.getHttpAccessControl());
	}

	@Override
	public void linkSupportingManagedObject(O dependency,
			HttpSecuritySupportingManagedObject<?> supportingManagedObject) {
		this.link(dependency, (context) -> context.getSupportingManagedObject(supportingManagedObject));

	}

	/**
	 * Links the {@link HttpSecuritySupportingManagedObjectDependencyType}.
	 * 
	 * @param key       Dependency key.
	 * @param extractor Extracts the {@link OfficeManagedObject}.
	 */
	private void link(O key,
			Function<HttpSecuritySupportingManagedObjectDependencyContext, OfficeManagedObject> extractor) {
		this.dependencies.add(new HttpSecuritySupportingManagedObjectDependencyTypeImpl(key, extractor));
	}

	/*
	 * ============= HttpSecuritySupportingManagedObjectType ============
	 */

	@Override
	public String getSupportingManagedObjectName() {
		return this.name;
	}

	@Override
	public ManagedObjectSource<O, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public PropertyList getProperties() {
		return this.propertyList;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HttpSecuritySupportingManagedObjectDependencyType<O>[] getDependencyTypes() {
		return this.dependencies
				.toArray(new HttpSecuritySupportingManagedObjectDependencyType[this.dependencies.size()]);
	}

	/**
	 * {@link HttpSecuritySupportingManagedObjectDependencyType} implementation.
	 */
	private class HttpSecuritySupportingManagedObjectDependencyTypeImpl
			implements HttpSecuritySupportingManagedObjectDependencyType<O> {

		/**
		 * Key.
		 */
		private final O key;

		/**
		 * Extracts the {@link OfficeManagedObject} from the
		 * {@link HttpSecuritySupportingManagedObjectDependencyContext}.
		 */
		private final Function<HttpSecuritySupportingManagedObjectDependencyContext, OfficeManagedObject> extractor;

		/**
		 * Instantiate.
		 * 
		 * @param key       Key.
		 * @param extractor Extracts the {@link OfficeManagedObject} from the
		 *                  {@link HttpSecuritySupportingManagedObjectDependencyContext}.
		 */
		public HttpSecuritySupportingManagedObjectDependencyTypeImpl(O key,
				Function<HttpSecuritySupportingManagedObjectDependencyContext, OfficeManagedObject> extractor) {
			this.key = key;
			this.extractor = extractor;
		}

		/*
		 * ============ HttpSecuritySupportingManagedObjectDependencyType ============
		 */

		@Override
		public O getKey() {
			return this.key;
		}

		@Override
		public OfficeManagedObject getOfficeManagedObject(
				HttpSecuritySupportingManagedObjectDependencyContext context) {
			return this.extractor.apply(context);
		}
	}

}
