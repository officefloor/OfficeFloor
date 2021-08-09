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

package net.officefloor.compile.spi.governance.source;


/**
 * Specification of a {@link GovernanceSource}. This is different to the
 * {@link GovernanceSourceMetaData} as it specifies how to configure the
 * {@link GovernanceSource} to then obtain its
 * {@link GovernanceSource} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link GovernanceSource}.
	 * 
	 * @return Property specification.
	 */
	GovernanceSourceProperty[] getProperties();

}
