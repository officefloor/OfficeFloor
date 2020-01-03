/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link FlowCallback} that propagates failures and only handles success.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FlowSuccessful extends FlowCallback {

	/**
	 * Default implementation of {@link FlowCallback} to escalate and then invoke
	 * successful handling.
	 */
	default void run(Throwable escalation) throws Throwable {

		// Ensure propagate flow failure
		if (escalation != null) {
			throw escalation;
		}

		// Successful flow
		this.run();
	}

	/**
	 * Invoked on completion of successful {@link Flow}.
	 * 
	 * @throws Throwable Possible failure in handling completion.
	 */
	void run() throws Throwable;
}
