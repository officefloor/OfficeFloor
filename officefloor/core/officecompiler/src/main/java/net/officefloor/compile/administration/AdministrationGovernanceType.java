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

package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * <code>Type definition</code> of a {@link Governance} used by
 * {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the {@link Governance}.
	 * 
	 * @return Index identifying the {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the {@link Governance}.
	 * 
	 * @return Key identifying the {@link Governance}.
	 */
	F getKey();

}
