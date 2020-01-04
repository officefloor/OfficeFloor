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

package net.officefloor.compile.spi.pool.source;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Sources a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	ManagedObjectPoolSourceSpecification getSpecification();

	/**
	 * Initialises and configures the {@link ManagedObjectPoolSource}.
	 * 
	 * @param context
	 *            {@link ManagedObjectPoolSourceContext}.
	 * @return {@link ManagedObjectPoolSourceMetaData} for the
	 *         {@link ManagedObjectPool}.
	 * @throws Exception
	 *             If fails to configure the {@link ManagedObjectPoolSource}.
	 */
	ManagedObjectPoolSourceMetaData init(ManagedObjectPoolSourceContext context) throws Exception;

}
