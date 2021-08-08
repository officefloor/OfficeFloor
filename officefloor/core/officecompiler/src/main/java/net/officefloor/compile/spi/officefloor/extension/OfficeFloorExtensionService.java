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

package net.officefloor.compile.spi.officefloor.extension;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Enables plug-in extension of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorExtensionService {

	/**
	 * Extends the {@link OfficeFloor}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorExtensionContext}.
	 * @throws Exception
	 *             If fails to extend the {@link OfficeFloor}.
	 */
	void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception;

}
