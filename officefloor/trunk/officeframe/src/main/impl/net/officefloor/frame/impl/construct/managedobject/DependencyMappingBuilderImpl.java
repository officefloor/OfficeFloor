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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link DependencyMappingBuilder} implementation.
 * 
 * @author Daniel
 */
public class DependencyMappingBuilderImpl<D extends Enum<D>> implements
		DependencyMappingBuilder, ManagedObjectConfiguration<D> {

	/**
	 * Name of the {@link ManagedObject} is being bound.
	 */
	private final String boundManagedObjectName;

	/**
	 * Name of the {@link ManagedObject} within the {@link Office}.
	 */
	private final String officeManagedObjectName;

	/**
	 * {@link ManagedObjectDependencyConfiguration} by index of dependency.
	 */
	private final Map<Integer, ManagedObjectDependencyConfiguration<D>> dependencies = new HashMap<Integer, ManagedObjectDependencyConfiguration<D>>();

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name of the {@link ManagedObject} is being bound.
	 * @param officeManagedObjectName
	 *            Name of the {@link ManagedObject} within the {@link Office}.
	 */
	public DependencyMappingBuilderImpl(String boundManagedObjectName,
			String officeManagedObjectName) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.officeManagedObjectName = officeManagedObjectName;
	}

	/*
	 * ============= DependencyMappingBuilder =============================
	 */

	@Override
	public <d extends Enum<d>> void mapDependency(d key,
			String scopeManagedObjectName) {
		// Use ordinal of key to index the dependency
		this.mapDependency(key.ordinal(), key, scopeManagedObjectName);
	}

	@Override
	public void mapDependency(int index, String scopeManagedObjectName) {
		this.mapDependency(index, (D) null, scopeManagedObjectName);
	}

	/**
	 * Maps in the dependency.
	 * 
	 * @param index
	 *            Index to map the dependency under.
	 * @param key
	 *            Key for the dependency. May be <code>null</code>.
	 * @param scopeManagedObjectName
	 *            Scope name for the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	private <d extends Enum<d>> void mapDependency(int index, d key,
			String scopeManagedObjectName) {

		// Cast key to expected type
		D castKey = (D) key;

		// Create the dependency
		ManagedObjectDependencyConfigurationImpl dependency = new ManagedObjectDependencyConfigurationImpl(
				castKey, scopeManagedObjectName);

		// Map the dependency at the index
		this.dependencies.put(new Integer(index), dependency);
	}

	/*
	 * ================ WorkManagedObjectConfiguration ====================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	@Override
	public String getOfficeManagedObjectName() {
		return this.officeManagedObjectName;
	}

	@Override
	public ManagedObjectDependencyConfiguration<D>[] getDependencyConfiguration() {
		return ConstructUtil.toArray(this.dependencies,
				new ManagedObjectDependencyConfiguration[0]);
	}

	/**
	 * {@link ManagedObjectDependencyConfiguration} implementation .
	 */
	private class ManagedObjectDependencyConfigurationImpl implements
			ManagedObjectDependencyConfiguration<D> {

		/**
		 * Dependency key.
		 */
		private final D dependencyKey;

		/**
		 * {@link ManagedObject} name.
		 */
		private final String managedObjectName;

		/**
		 * Initiate.
		 * 
		 * @param dependencyKey
		 *            Dependency key.
		 * @param managedObjectName
		 *            {@link ManagedObject} name.
		 */
		public ManagedObjectDependencyConfigurationImpl(D dependencyKey,
				String managedObjectName) {
			this.dependencyKey = dependencyKey;
			this.managedObjectName = managedObjectName;
		}

		/*
		 * ============= ManagedObjectDependencyConfiguration ================
		 */

		@Override
		public D getDependencyKey() {
			return this.dependencyKey;
		}

		@Override
		public String getScopeManagedObjectName() {
			return this.managedObjectName;
		}
	}

}