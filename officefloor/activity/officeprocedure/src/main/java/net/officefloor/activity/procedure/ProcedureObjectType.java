/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure;

/**
 * <code>Type definition</code> of a dependent {@link Object} required by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureObjectType {

	/**
	 * Obtains the name for the {@link ProcedureObjectType}.
	 * 
	 * @return Name for the {@link ProcedureObjectType}.
	 */
	String getObjectName();

	/**
	 * Obtains the required type of the dependent {@link Object}.
	 * 
	 * @return Required type of the dependent {@link Object}.
	 */
	Class<?> getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

}
