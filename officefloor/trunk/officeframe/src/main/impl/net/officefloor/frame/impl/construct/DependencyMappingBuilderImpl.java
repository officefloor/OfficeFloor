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
package net.officefloor.frame.impl.construct;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;

/**
 * Implementation of the
 * {@link net.officefloor.frame.api.build.DependencyMappingBuilder}.
 * 
 * @author Daniel
 */
public class DependencyMappingBuilderImpl implements DependencyMappingBuilder,
		ManagedObjectConfiguration {

	/**
	 * Name of this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} local to
	 * the {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final String workManagedObjectName;

	/**
	 * Id of the {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected final String managedObjectId;

	/**
	 * Dependency mappings.
	 */
	protected final List<ManagedObjectDependencyConfigurationImpl<?>> dependencies = new LinkedList<ManagedObjectDependencyConfigurationImpl<?>>();

	/**
	 * Initiate.
	 * 
	 * @param workManagedObjectName
	 *            Name of this
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            local to the {@link net.officefloor.frame.api.execute.Work}.
	 * @param managedObjectId
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            to map in dependencies.
	 */
	public DependencyMappingBuilderImpl(String workManagedObjectName,
			String managedObjectId) {
		this.workManagedObjectName = workManagedObjectName;
		this.managedObjectId = managedObjectId;
	}

	/*
	 * ====================================================================
	 * DependencyMappingBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.DependencyMappingBuilder#registerDependencyMapping(java.lang.Enum,
	 *      java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void registerDependencyMapping(Enum key, String managedObjectId)
			throws BuildException {
		this.dependencies.add(new ManagedObjectDependencyConfigurationImpl(key,
				managedObjectId));
	}

	/*
	 * ====================================================================
	 * WorkManagedObjectConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkManagedObjectConfiguration#getWorkManagedObjectName()
	 */
	public String getManagedObjectName() {
		return this.workManagedObjectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkManagedObjectConfiguration#getManagedObjectId()
	 */
	public String getManagedObjectId() {
		return this.managedObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkManagedObjectConfiguration#getTimeout()
	 */
	public long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkManagedObjectConfiguration#getDependencyConfiguration()
	 */
	public ManagedObjectDependencyConfiguration<?>[] getDependencyConfiguration() {
		return this.dependencies
				.toArray(new ManagedObjectDependencyConfiguration[0]);
	}

}

/**
 * Implementation of
 * {@link net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration}.
 */
class ManagedObjectDependencyConfigurationImpl<D extends Enum<D>> implements
		ManagedObjectDependencyConfiguration<D> {

	/**
	 * Dependency key.
	 */
	private final D dependencyKey;

	/**
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} name.
	 */
	private final String managedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param dependencyKey
	 *            Dependency key.
	 * @param managedObjectName
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            name.
	 */
	public ManagedObjectDependencyConfigurationImpl(D dependencyKey,
			String managedObjectName) {
		this.dependencyKey = dependencyKey;
		this.managedObjectName = managedObjectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration#getDependencyKey()
	 */
	public D getDependencyKey() {
		return this.dependencyKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration#getManagedObjectName()
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

}