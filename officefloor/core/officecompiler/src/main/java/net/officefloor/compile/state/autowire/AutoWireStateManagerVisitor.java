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

package net.officefloor.compile.state.autowire;

import net.officefloor.frame.api.manage.Office;

/**
 * Visitor for the {@link AutoWireStateManager} of each {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManagerVisitor {

	/**
	 * Visits the {@link AutoWireStateManagerFactory} for the {@link Office}.
	 * 
	 * @param officeName                  Name of the {@link Office}.
	 * @param autoWireStateManagerFactory {@link AutoWireStateManagerFactory}.
	 * @throws Exception If fails to visit the {@link AutoWireStateManagerFactory}.
	 */
	void visit(String officeName, AutoWireStateManagerFactory autoWireStateManagerFactory) throws Exception;

}
