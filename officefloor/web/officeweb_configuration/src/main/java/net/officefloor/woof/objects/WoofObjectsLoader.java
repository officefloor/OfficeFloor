/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.objects;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.woof.model.objects.WoofObjectsModel;

/**
 * Loads the {@link WoofObjectsModel} and configures the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofObjectsLoader {

	/**
	 * Loads the {@link WoofObjectsModel} configuration and configures the
	 * {@link SupplierSource}.
	 * 
	 * @param context
	 *            {@link WoofObjectsLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofObjectsConfiguration(WoofObjectsLoaderContext context) throws Exception;

}
