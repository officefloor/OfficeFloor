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

package net.officefloor.compile.classes;

import java.io.IOException;

/**
 * <p>
 * Allows enhancing the {@link Class} path scanning with custom scanning.
 * <p>
 * This is useful if specific {@link ClassLoader} instances are used that
 * require custom logic to scan them.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassPathScanner {

	/**
	 * Scans the class path.
	 * 
	 * @param context {@link ClassPathScannerContext}.
	 * @throws IOException If failure in scanning the class path.
	 */
	void scan(ClassPathScannerContext context) throws IOException;

}
