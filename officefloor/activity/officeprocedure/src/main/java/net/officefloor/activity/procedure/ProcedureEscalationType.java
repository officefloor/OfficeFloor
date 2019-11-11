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
package net.officefloor.activity.procedure;

import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureEscalationType {

	/**
	 * Obtains the name for the {@link ProcedureEscalationType}.
	 * 
	 * @return Name for the {@link ProcedureEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the {@link Procedure}.
	 * 
	 * @return Type of {@link EscalationFlow} by the {@link Procedure}.
	 */
	Class<? extends Throwable> getEscalationType();

}