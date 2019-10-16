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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * <code>Type definition</code> of a {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureType {

	/**
	 * Obtains the name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * Obtains the type name of the {@link Parameter}.
	 * 
	 * @return Type name of the {@link Parameter}. May be <code>null</code> if no
	 *         {@link Parameter}.
	 */
	Class<?> getParameterType();

	/**
	 * Obtains the {@link ProcedureObjectType} definitions for the dependent
	 * {@link Object} instances required by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureObjectType} definitions for the dependent
	 *         {@link Object} instances required by the {@link Procedure}.
	 */
	ProcedureObjectType[] getObjectTypes();

	/**
	 * Obtains the {@link ProcedureFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link Procedure}.
	 */
	ProcedureFlowType[] getFlowTypes();

	/**
	 * Obtains the {@link ProcedureEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Procedure}.
	 */
	ProcedureEscalationType[] getEscalationTypes();

	/**
	 * Obtains the type name of {@link Object} passed to the {@link Next}
	 * {@link ManagedFunction}.
	 * 
	 * @return Type name of {@link Object} passed to the {@link Next}
	 *         {@link ManagedFunction}. May be <code>null</code> if no argument.
	 */
	Class<?> getNextArgumentType();

}