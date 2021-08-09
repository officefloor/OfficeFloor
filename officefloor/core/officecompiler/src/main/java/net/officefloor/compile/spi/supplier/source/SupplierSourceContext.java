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

package net.officefloor.compile.spi.supplier.source;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSourceContext extends SupplierCompileContext, SourceContext {

	/**
	 * Adds a {@link SupplierCompileCompletion} to be invoked after compilation of
	 * respective {@link OfficeFloor} / {@link Office} functionality.
	 * 
	 * @param completion {@link SupplierCompileCompletion}.
	 */
	void addCompileCompletion(SupplierCompileCompletion completion);

}
