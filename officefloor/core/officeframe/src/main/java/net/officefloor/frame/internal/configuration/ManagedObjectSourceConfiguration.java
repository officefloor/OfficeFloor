/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Configuration of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceConfiguration<F extends Enum<F>, MS extends ManagedObjectSource<?, F>> {

	/**
	 * Obtains the name of this {@link ManagedObjectSource}.
	 * 
	 * @return Name of this {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link ManagedObjectSource} instance to use.
	 * 
	 * @return {@link ManagedObjectSource} instance to use. This may be
	 *         <code>null</code> and therefore the
	 *         {@link #getManagedObjectSourceClass()} should be used to obtain the
	 *         {@link ManagedObjectSource}.
	 */
	MS getManagedObjectSource();

	/**
	 * Obtains the {@link Class} of the {@link ManagedObjectSource}.
	 * 
	 * @return {@link Class} of the {@link ManagedObjectSource}. Will be
	 *         <code>null</code> if a {@link ManagedObjectSource} instance is
	 *         configured.
	 */
	Class<MS> getManagedObjectSourceClass();

	/**
	 * Obtains the additional profiles.
	 * 
	 * @return Additional profiles.
	 */
	String[] getAdditionalProfiles();

	/**
	 * Obtains the {@link SourceProperties} to initialise the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link SourceProperties} to initialise the
	 *         {@link ManagedObjectSource}.
	 */
	SourceProperties getProperties();

	/**
	 * Obtains the {@link ManagingOfficeConfiguration} detailing the {@link Office}
	 * responsible for managing this {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagingOfficeConfiguration}.
	 */
	ManagingOfficeConfiguration<F> getManagingOfficeConfiguration();

	/**
	 * Obtains the {@link ManagedObjectPoolConfiguration} for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectPoolConfiguration} for this
	 *         {@link ManagedObjectSource} or <code>null</code> if not to be pooled.
	 */
	ManagedObjectPoolConfiguration getManagedObjectPoolConfiguration();

	/**
	 * Obtains the timeout to:
	 * <ol>
	 * <li>to source the {@link ManagedObject}</li>
	 * <li>have asynchronous operations on the {@link ManagedObject} complete</li>
	 * </ol>
	 * 
	 * @return Timeout.
	 */
	long getTimeout();

}
