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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;

/**
 * Builds the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceBuilder<F extends Enum<F>> extends FunctionBuilder<F> {

	/**
	 * Specifies the timeout to for {@link AsynchronousFlow} instances for this
	 * {@link Governance}.
	 *
	 * @param timeout Timeout.
	 */
	void setAsynchronousFlowTimeout(long timeout);

}
