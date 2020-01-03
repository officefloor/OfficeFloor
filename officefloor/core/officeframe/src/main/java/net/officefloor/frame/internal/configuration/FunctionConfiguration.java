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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;

/**
 * Configuration of a {@link ManagedFunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Team} to execute the
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return Name of {@link Team}. May be <code>null</code> to use any
	 *         {@link Team}.
	 */
	String getResponsibleTeamName();

	/**
	 * Obtains the configuration of the {@link Flow} instances for this
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return Configuration of {@link Flow} instances for this
	 *         {@link ManagedFunctionLogic}.
	 */
	FlowConfiguration<F>[] getFlowConfiguration();

	/**
	 * Obtains the {@link EscalationConfiguration} instances.
	 * 
	 * @return {@link EscalationConfiguration} instances.
	 */
	EscalationConfiguration[] getEscalations();

}
