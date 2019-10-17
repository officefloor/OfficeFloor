/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link ProcedureEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEscalationTypeImpl implements ProcedureEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of {@link Escalation}.
	 */
	private final Class<? extends Throwable> escalationType;

	/**
	 * Instantiate.
	 * 
	 * @param escalationName Name of {@link EscalationFlow}.
	 * @param escalationType Type of {@link Escalation}.
	 */
	public ProcedureEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =============== ProcedureEscalationType ===================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	public Class<? extends Throwable> getEscalationType() {
		return this.escalationType;
	}
}