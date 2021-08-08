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

package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an {@link OfficeOutput} from the
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutputType {

	/**
	 * Obtains the name of {@link OfficeOutput}.
	 * 
	 * @return Name of this {@link OfficeOutput}.
	 */
	String getOfficeOutputName();

	/**
	 * Obtains the fully qualified class name of the argument type from this
	 * {@link OfficeOutput}.
	 * 
	 * @return Argument type to this {@link OfficeOutput}.
	 */
	String getArgumentType();

}
