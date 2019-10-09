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
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * Builds the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureDesigner {

	/**
	 * Adds a {@link Procedure}.
	 * 
	 * @param className     Name of {@link Class}.
	 * @param serviceName   {@link ProcedureService} name.
	 * @param procedureName Name of {@link Procedure}.
	 * @return {@link SectionFunction} for the {@link Procedure}.
	 */
	SectionFunction addProcedure(String className, String serviceName, String procedureName);

	/**
	 * Informs the {@link SectionDesigner} of the {@link Procedure} instances. This
	 * is invoked once all {@link Procedure} instances are added.
	 */
	void informSectionDesigner();

}