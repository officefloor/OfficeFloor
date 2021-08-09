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

package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTeamType {

	/**
	 * Obtains the name to identify requirement of a {@link Team}.
	 * 
	 * @return Name to identify requirement of a {@link Team}.
	 */
	String getTeamName();

}
