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

package net.officefloor.frame.test;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * <p>
 * Test support object.
 * <p>
 * {@link TestSupportExtension} will invoke this to enable the
 * {@link TestSupport} instance to initialise itself.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestSupport {

	/**
	 * Intialise.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @throws Exception If fails to init.
	 */
	void init(ExtensionContext context) throws Exception;

}
