/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link GovernanceEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceEscalationTypeImpl implements GovernanceEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of the {@link EscalationFlow}.
	 */
	private final Class<?> escalationType;

	/**
	 * Initiate.
	 * 
	 * @param escalationName
	 *            Name of {@link EscalationFlow}.
	 * @param escalationType
	 *            Type of the {@link EscalationFlow}.
	 */
	public GovernanceEscalationTypeImpl(String escalationName,
			Class<?> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =================== GovernanceEscalationType ==========================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

}