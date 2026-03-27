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

package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.api.manage.Office;

/**
 * Extension for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context {@link CompileOfficeContext}.
	 * @throws Exception If fails to extend.
	 */
	void extend(CompileOfficeContext context) throws Exception;

	/**
	 * Creates a {@link CompileOfficeExtension} from an
	 * {@link OfficeExtensionService}.
	 * 
	 * @param officeExtensionService {@link OfficeExtensionService}.
	 * @return {@link CompileOfficeExtension} wrapping the
	 *         {@link OfficeExtensionService}.
	 */
	static CompileOfficeExtension of(OfficeExtensionService officeExtensionService) {
		return (extension) -> {
			OfficeArchitect office = extension.getOfficeArchitect();
			OfficeExtensionContext context = (OfficeExtensionContext) extension.getOfficeSourceContext();
			officeExtensionService.extendOffice(office, context);
		};
	}
}
