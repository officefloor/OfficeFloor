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

import net.officefloor.activity.impl.procedure.ProcedureLoaderCompilerRunnable;
import net.officefloor.activity.impl.procedure.ProcedureLoaderImpl;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.section.ProcedureSectionSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Employs {@link ProcedureArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEmployer {

	/**
	 * Creates the {@link ProcedureLoader}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link ProcedureLoader}.
	 * @throws Exception If fails to create {@link ProcedureLoader}.
	 */
	public static ProcedureLoader employProcedureLoader(OfficeFloorCompiler compiler) throws Exception {
		return compiler.run(ProcedureLoaderCompilerRunnable.class);
	}

	/**
	 * Creates the {@link ProcedureLoader}.
	 * 
	 * @param designer {@link SectionDesigner}.
	 * @param context  {@link SectionSourceContext}.
	 * @return {@link ProcedureLoader}.
	 * @throws Exception If fails to create {@link ProcedureLoader}.
	 */
	public static ProcedureLoader employProcedureLoader(SectionDesigner designer, SectionSourceContext context)
			throws Exception {
		return new ProcedureLoaderImpl(designer, context);
	}

	/**
	 * Employs the {@link ProcedureArchitect}.
	 * 
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 * @return {@link ProcedureArchitect}.
	 */
	public static ProcedureArchitect<OfficeSection> employProcedureArchitect(OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new ProcedureArchitect<OfficeSection>() {

			@Override
			public OfficeSection addProcedure(String className, String serviceName, String procedureName,
					boolean isNext) {
				OfficeSection procedure = officeArchitect.addOfficeSection(procedureName,
						ProcedureSectionSource.class.getName(), procedureName);
				procedure.addProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, className);
				procedure.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME, serviceName);
				if (isNext) {
					procedure.addProperty(ProcedureSectionSource.IS_NEXT_PROPERTY_NAME, Boolean.TRUE.toString());
				}
				return procedure;
			}
		};
	}

	/**
	 * Employs the {@link ProcedureArchitect}.
	 * 
	 * @param sectionDesigner      {@link SectionDesigner}.
	 * @param sectionSourceContext {@link SectionSourceContext}.
	 * @return {@link ProcedureArchitect}.
	 */
	public static ProcedureArchitect<SubSection> employProcedureDesigner(SectionDesigner sectionDesigner,
			SectionSourceContext sectionSourceContext) {
		return new ProcedureArchitect<SubSection>() {

			@Override
			public SubSection addProcedure(String className, String serviceName, String procedureName, boolean isNext) {
				SubSection procedure = sectionDesigner.addSubSection(procedureName,
						ProcedureSectionSource.class.getName(), procedureName);
				procedure.addProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, className);
				procedure.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME, serviceName);
				if (isNext) {
					procedure.addProperty(ProcedureSectionSource.IS_NEXT_PROPERTY_NAME, Boolean.TRUE.toString());
				}
				return procedure;
			}
		};
	}

}