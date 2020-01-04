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

package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.AdministrationMetaData;

/**
 * Describes a {@link Governance} used by the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceMetaData<G extends Enum<G>> {

	/**
	 * Obtains the {@link Enum} key identifying this {@link Governance}. If
	 * <code>null</code> then {@link Governance} will be referenced by this
	 * instance's index in the array returned from
	 * {@link AdministrationMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Governance} or
	 *         <code>null</code> indicating identified by an index.
	 */
	G getKey();

	/**
	 * Provides a descriptive name for this {@link Escalation}. This is useful
	 * to better describe the {@link Escalation}.
	 * 
	 * @return Descriptive name for this {@link Escalation}.
	 */
	String getLabel();

}
