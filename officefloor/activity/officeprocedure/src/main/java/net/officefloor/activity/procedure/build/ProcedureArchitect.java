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
package net.officefloor.activity.procedure.build;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ProcedureService;
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
	 * @param className     Name of {@link Class}.
	 * @param serviceName   {@link ProcedureService} name.
	 * @param procedureName Name of {@link Procedure}.
	 * @param isNext        Indicates if next {@link Flow} configured.
	 * @return {@link OfficeSection}/{@link SubSection} for the {@link Procedure}.
	 */
	S addProcedure(String className, String serviceName, String procedureName, boolean isNext);

}