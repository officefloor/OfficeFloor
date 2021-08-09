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

package net.officefloor.frame.api.managedobject.source;

/**
 * Specification of a {@link ManagedObjectSource}. This is different to the
 * {@link ManagedObjectSourceMetaData} as it specifies how to configure the
 * {@link ManagedObjectSource} to then obtain its
 * {@link ManagedObjectSourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Property specification.
	 */
	ManagedObjectSourceProperty[] getProperties();
}
