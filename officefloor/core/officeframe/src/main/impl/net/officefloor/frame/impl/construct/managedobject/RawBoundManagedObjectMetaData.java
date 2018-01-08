/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.managedobject;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Raw meta-data for a bound {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaData {

	/**
	 * Name that the {@link ManagedObject} is bound under.
	 */
	private final String boundManagedObjectName;

	/**
	 * Indicates if an Input {@link ManagedObject}.
	 */
	private final boolean isInput;

	/**
	 * Bound {@link ManagedObjectIndex} for the {@link ManagedObject}.
	 */
	private ManagedObjectIndex index = null;

	/**
	 * Listing of {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * this {@link RawBoundManagedObjectMetaData}.
	 */
	final List<RawBoundManagedObjectInstanceMetaData<?>> instancesMetaData = new LinkedList<RawBoundManagedObjectInstanceMetaData<?>>();

	/**
	 * Default instance index.
	 */
	int defaultInstanceIndex = -1;

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param isInput
	 *            Indicates if an Input {@link ManagedObject}.
	 */
	public RawBoundManagedObjectMetaData(String boundManagedObjectName, boolean isInput) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.isInput = isInput;
	}

	/**
	 * Indicates if input.
	 * 
	 * @return <code>true</code> if input.
	 */
	public boolean isInput() {
		return this.isInput;
	}

	/**
	 * Specifies the {@link ManagedObjectIndex}.
	 * 
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param indexOfManagedObjectWithinScope
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public void setManagedObjectIndex(ManagedObjectScope managedObjectScope, int indexOfManagedObjectWithinScope) {
		this.index = new ManagedObjectIndexImpl(managedObjectScope, indexOfManagedObjectWithinScope);
	}

	/**
	 * Adds a {@link RawBoundManagedObjectInstanceMetaData} to this
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependenciesConfiguration
	 *            Listing of the {@link ManagedObjectDependencyConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceConfiguration
	 *            Listing of the {@link ManagedObjectGovernanceConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param preloadAdministration
	 *            Listing of the pre-load {@link AdministrationConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @return {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	public RawBoundManagedObjectInstanceMetaData<?> addInstance(String boundManagedObjectName,
			RawManagedObjectMetaData<?, ?> rawMoMetaData,
			ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration,
			ManagedObjectGovernanceConfiguration[] governanceConfiguration,
			AdministrationConfiguration<?, ?, ?>[] preloadAdministration) {

		// Obtain the index for the instance
		int instanceIndex = this.instancesMetaData.size();

		// Add the instance meta-data
		RawBoundManagedObjectInstanceMetaData<?> instance = new RawBoundManagedObjectInstanceMetaData<>(
				boundManagedObjectName, this, instanceIndex, rawMoMetaData, dependenciesConfiguration,
				governanceConfiguration, preloadAdministration);
		this.instancesMetaData.add(instance);
		return instance;
	}

	/**
	 * Obtains the name the {@link ManagedObject} is bound under.
	 *
	 * @return Name the {@link ManagedObject} is bound under.
	 */
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	/**
	 * Obtains the {@link ManagedObjectIndex}.
	 *
	 * @return {@link ManagedObjectIndex}.
	 */
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.index;
	}

	/**
	 * Obtains the index of the default
	 * {@link RawBoundManagedObjectInstanceMetaData} for this
	 * {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return Index of the default
	 *         {@link RawBoundManagedObjectInstanceMetaData} for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	public int getDefaultInstanceIndex() {
		return this.defaultInstanceIndex;
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * the {@link ManagedObjectSource} instances that may provide a
	 * {@link ManagedObject} for this {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return {@link RawBoundManagedObjectMetaData} instances for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	public RawBoundManagedObjectInstanceMetaData<?>[] getRawBoundManagedObjectInstanceMetaData() {
		// Provide the instances in order of their indexes
		return this.instancesMetaData.toArray(new RawBoundManagedObjectInstanceMetaData[0]);
	}

}