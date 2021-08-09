/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * <code>Type definition</code> of the {@link OfficeSectionOutput}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionOutputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link OfficeSectionOutput}.
	 * 
	 * @return Name of this {@link OfficeSectionOutput}.
	 */
	String getOfficeSectionOutputName();

	/**
	 * Obtains the argument type from this {@link OfficeSectionOutput}.
	 * 
	 * @return Argument type.
	 */
	String getArgumentType();

	/**
	 * Indicates if this {@link OfficeSectionOutput} is escalation only. In other
	 * words it can be handled by an {@link Office} {@link EscalationProcedure}.
	 * 
	 * @return <code>true</code> if escalation only.
	 */
	boolean isEscalationOnly();

}
