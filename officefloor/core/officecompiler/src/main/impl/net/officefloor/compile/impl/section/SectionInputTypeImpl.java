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

package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.section.SectionInput;

/**
 * {@link SectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionInputTypeImpl implements SectionInputType {

	/**
	 * Name of the {@link SectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link SectionInput}.
	 */
	private final String parameterType;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link SectionInput}.
	 * @param annotations
	 *            Annotations.
	 */
	public SectionInputTypeImpl(String inputName, String parameterType, Object[] annotations) {
		this.inputName = inputName;
		this.parameterType = parameterType;
		this.annotations = annotations;
	}

	/*
	 * ==================== SectionInputType =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}
