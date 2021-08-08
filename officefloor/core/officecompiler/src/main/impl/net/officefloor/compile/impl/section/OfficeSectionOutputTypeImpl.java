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

package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link OfficeSectionOutputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionOutputTypeImpl implements OfficeSectionOutputType {

	/**
	 * Name of the {@link OfficeSectionOutput}.
	 */
	private final String outputName;

	/**
	 * Argument type for the {@link OfficeSectionOutput}.
	 */
	private final String argumentType;

	/**
	 * Flag indicating if {@link OfficeSectionOutput} is {@link Escalation}
	 * only.
	 */
	private final boolean isEscalationOnly;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @param argumentType
	 *            Argument type for the {@link OfficeSectionOutput}.
	 * @param isEscalationOnly
	 *            Flag indicating if {@link OfficeSectionOutput} is
	 *            {@link Escalation} only.
	 * @param annotations
	 *            Annotations.
	 */
	public OfficeSectionOutputTypeImpl(String outputName, String argumentType, boolean isEscalationOnly,
			Object[] annotations) {
		this.outputName = outputName;
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
		this.annotations = annotations;
	}

	/*
	 * =================== OfficeSectionOutputType =======================
	 */

	@Override
	public String getOfficeSectionOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	@Override
	public boolean isEscalationOnly() {
		return this.isEscalationOnly;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}
