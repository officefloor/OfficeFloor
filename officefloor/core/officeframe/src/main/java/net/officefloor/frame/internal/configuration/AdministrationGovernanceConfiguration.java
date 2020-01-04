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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * Configuration of linking {@link Governance} to {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceConfiguration<G extends Enum<G>> {

	/**
	 * Obtains the name of the {@link Governance} to link to the
	 * {@link Administration}.
	 * 
	 * @return Name of the {@link Governance} to link to the
	 *         {@link Administration}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the linked {@link Governance}.
	 * 
	 * @return Index identifying the linked {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the linked {@link Governance}.
	 * 
	 * @return Key identifying the linked {@link Governance}. <code>null</code>
	 *         if indexed.
	 */
	G getKey();

}
