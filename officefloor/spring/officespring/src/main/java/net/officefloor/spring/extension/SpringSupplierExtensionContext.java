/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring.extension;

import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * Context for a {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringSupplierExtensionContext {

	/**
	 * Obtains the object source from a {@link ManagedObject}.
	 * 
	 * @param <O>        Object type.
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type required.
	 * @return Object from the {@link ManagedObject}.
	 * @throws Exception If fails to source the {@link ManagedObject}.
	 */
	<O> O getManagedObject(String qualifier, Class<? extends O> objectType) throws Exception;

	/**
	 * Registers a {@link ThreadSynchroniserFactory}.
	 * 
	 * @param threadSynchroniserFactory {@link ThreadSynchroniserFactory}.
	 */
	void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory);

	/**
	 * Obtains the {@link AvailableType} instances from {@link OfficeFloor}.
	 * 
	 * @return {@link AvailableType} instances from {@link OfficeFloor}.
	 */
	AvailableType[] getAvailableTypes();

}
