/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.activity;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureTypeBuilder;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.compile.test.section.SectionTypeBuilder;

/**
 * Trait for testing the Activity.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityTestTrait {

	/**
	 * Constructs an {@link SectionType} for testing.
	 * 
	 * @param constructor {@link SectionTypeConstructor}.
	 * @return {@link SectionType}.
	 */
	public default SectionType constructSectionType(SectionTypeConstructor constructor) {

		// Construct and return the office section
		SectionTypeBuilder builder = SectionLoaderUtil.createSectionTypeBuilder();
		if (constructor != null) {
			constructor.construct(builder);
		}
		return SectionLoaderUtil.buildSectionType(builder.getSectionDesigner());
	}

	/**
	 * Constructor of an {@link SectionType}.
	 */
	@FunctionalInterface
	interface SectionTypeConstructor {

		/**
		 * Constructs the {@link SectionType}.
		 * 
		 * @param builder {@link SectionType}.
		 */
		void construct(SectionTypeBuilder builder);
	}

	/**
	 * Constructs the {@link ProcedureType} for testing.
	 * 
	 * @param procedureName {@link Procedure} name.
	 * @param parameterType Parameter type.
	 * @param constructor   {@link ProcedureTypeConstructor}.
	 * @return {@link ProcedureType}.
	 */
	public default ProcedureType constructProcedureType(String procedureName, Class<?> parameterType,
			ProcedureTypeConstructor constructor) {

		// Construct and return the procedure
		ProcedureTypeBuilder builder = ProcedureLoaderUtil.createProcedureTypeBuilder(procedureName, parameterType);
		if (constructor != null) {
			constructor.construct(builder);
		}
		return builder.build();
	}

	/**
	 * Constructor of an {@link ProcedureType}.
	 */
	@FunctionalInterface
	interface ProcedureTypeConstructor {

		/**
		 * Constructs the {@link ProcedureType}.
		 * 
		 * @param builder {@link ProcedureTypeBuilder}.
		 */
		void construct(ProcedureTypeBuilder builder);
	}

}
