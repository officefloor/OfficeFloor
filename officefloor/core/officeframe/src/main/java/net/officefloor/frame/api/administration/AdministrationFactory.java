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

package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Factory for the creation of an {@link Administration}.
 * 
 * @param <E>
 *            Extension interface used to administer the {@link ManagedObject}
 *            instances.
 * @param <F>
 *            {@link Flow} keys for invoked {@link Flow} instances from this
 *            {@link Administration}.
 * @param <G>
 *            {@link Governance} keys identifying the {@link Governance} that
 *            may be under {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationFactory<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Creates the {@link Administration}.
	 * 
	 * @return {@link Administration}.
	 * @throws Throwable
	 *             If fails to create {@link Administration}.
	 */
	Administration<E, F, G> createAdministration() throws Throwable;

}
