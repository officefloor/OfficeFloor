/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * {@link ProcessMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMetaDataImpl implements ProcessMetaData {

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} instances.
	 * @param threadMetaData        {@link ThreadMetaData}.
	 */
	public ProcessMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData, ThreadMetaData threadMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.threadMetaData = threadMetaData;
	}

	/*
	 * ============== ProcessMetaData =================================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

}
