/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.supply.extension;

import jakarta.servlet.Servlet;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * Extension to {@link ServletSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletSupplierExtension {

	/**
	 * <p>
	 * Invoked before completing.
	 * <p>
	 * Note that the {@link Servlet} container may have been force started before
	 * invoking this.
	 * 
	 * @param context {@link BeforeCompleteServletSupplierExtensionContext}.
	 * @throws Exception If fails completion.
	 */
	default void beforeCompletion(BeforeCompleteServletSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

}
