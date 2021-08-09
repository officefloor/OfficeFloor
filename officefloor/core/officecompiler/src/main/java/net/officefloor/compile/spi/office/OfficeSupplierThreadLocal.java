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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SupplierThreadLocal} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSupplierThreadLocal extends OfficeDependencyRequireNode {

	/**
	 * Obtains the name of this {@link OfficeSupplierThreadLocal}.
	 * 
	 * @return Name of this {@link OfficeSupplierThreadLocal}.
	 */
	String getOfficeSupplierThreadLocalName();

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the required type.
	 * 
	 * @return Required type.
	 */
	String getType();

}
