/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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