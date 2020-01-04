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

package net.officefloor.activity.procedure.build;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builds the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureArchitect<S> {

	/**
	 * {@link SectionInput} name to invoke the {@link Procedure}.
	 */
	public static final String INPUT_NAME = "procedure";

	/**
	 * {@link SectionOutput} name for next {@link Flow}.
	 */
	public static final String NEXT_OUTPUT_NAME = "NEXT";

	/**
	 * Adds a {@link Procedure}.
	 * 
	 * @param sectionName   Name to uniquely identify the use of the
	 *                      {@link Procedure}. It is possible to configure the same
	 *                      {@link Procedure}, so need to name them each uniquely.
	 * @param resource      Resource.
	 * @param sourceName    {@link ProcedureSource} name.
	 * @param procedureName Name of {@link Procedure} to be provided to
	 *                      {@link ProcedureSource}.
	 * @param isNext        Indicates if next {@link Flow} configured.
	 * @param properties    {@link PropertyList} for extra configuration.
	 * @return {@link OfficeSection}/{@link SubSection} for the {@link Procedure}.
	 */
	S addProcedure(String sectionName, String resource, String sourceName, String procedureName, boolean isNext,
			PropertyList properties);

}
