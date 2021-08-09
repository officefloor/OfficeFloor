/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.pool;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ManagedObjectPoolSource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolSourceContextImpl extends SourceContextImpl implements ManagedObjectPoolSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param managedObjectPoolName Name of the {@link ManagedObjectPool}.
	 * @param isLoadingType         Indicates if loading type.
	 * @param additionalProfiles    Additional profiles,
	 * @param properties            Properties.
	 * @param sourceContext         Delegate {@link SourceContext}.
	 */
	public ManagedObjectPoolSourceContextImpl(String managedObjectPoolName, boolean isLoadingType,
			String[] additionalProfiles, SourceProperties properties, SourceContext sourceContext) {
		super(managedObjectPoolName, isLoadingType, additionalProfiles, sourceContext, properties);
	}

}
