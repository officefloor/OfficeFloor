/*-
 * #%L
 * Spring Integration
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
