/*-
 * #%L
 * net.officefloor.gef.shade
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

package org.eclipse.swt;

/**
 * <p>
 * Avoiding importing SWT, which has O/S specific implementations.
 * <p>
 * Providing this only as necessary for GEF.
 * 
 * @author Daniel Sagenschneider
 */
public class SWTError extends Error {

	private static final long serialVersionUID = -9160311124292866538L;
}
