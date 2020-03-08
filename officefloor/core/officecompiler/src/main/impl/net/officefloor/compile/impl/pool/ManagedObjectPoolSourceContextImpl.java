/*-
 * #%L
 * OfficeCompiler
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
