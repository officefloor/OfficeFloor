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

package net.officefloor.compile.spi.office;

/**
 * Input into the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionInput extends OfficeFlowSinkNode {

	/**
	 * Obtains the {@link OfficeSection} containing this
	 * {@link OfficeSectionInput}.
	 * 
	 * @return {@link OfficeSection} containing this {@link OfficeSectionInput}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Obtains the name of this {@link OfficeSectionInput}.
	 * 
	 * @return Name of this {@link OfficeSectionInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Adds an {@link ExecutionExplorer} for the execution tree from this
	 * {@link OfficeSectionInput}.
	 * 
	 * @param executionExplorer
	 *            {@link ExecutionExplorer}.
	 */
	void addExecutionExplorer(ExecutionExplorer executionExplorer);

}
