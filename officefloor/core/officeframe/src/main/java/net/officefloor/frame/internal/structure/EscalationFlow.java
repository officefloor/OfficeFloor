/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link Flow} to handle an {@link Escalation} from a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationFlow {

	/**
	 * Obtains the type of cause handled by this {@link EscalationFlow}.
	 * 
	 * @return Type of cause handled by this {@link EscalationFlow}.
	 */
	Class<? extends Throwable> getTypeOfCause();

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the escalation handling
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of the escalation handling
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData();

}
