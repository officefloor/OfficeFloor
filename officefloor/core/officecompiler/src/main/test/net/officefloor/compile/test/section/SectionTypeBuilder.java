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
package net.officefloor.compile.test.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * Facade builder for the {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionTypeBuilder {

	/**
	 * Adds an {@link SectionInputType}.
	 * 
	 * @param name          Name.
	 * @param parameterType Parameter type.
	 */
	void addSectionInput(String name, Class<?> parameterType);

	/**
	 * Adds an {@link SectionOutputType}.
	 * 
	 * @param name             Name.
	 * @param argumentType     Argument type.
	 * @param isEscalationOnly Flag indicating if escalation only.
	 */
	void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

	/**
	 * Adds a non-{@link Escalation} {@link SectionOutputType}.
	 * 
	 * @param name         Name.
	 * @param argumentType Argument type.
	 */
	void addSectionOutput(String name, Class<?> argumentType);

	/**
	 * Adds an {@link Escalation} {@link SectionOutputType}.
	 * 
	 * @param escalationType {@link Escalation} type.
	 */
	void addSectionEscalation(Class<?> escalationType);

	/**
	 * Adds an {@link SectionObjectType}.
	 * 
	 * @param name          Name.
	 * @param objectType    Object type.
	 * @param typeQualifier Type qualifier.
	 */
	void addSectionObject(String name, Class<?> objectType, String typeQualifier);

	/**
	 * Obtains the underlying {@link SectionDesigner}.
	 * 
	 * @return Underlying {@link SectionDesigner}.
	 */
	SectionDesigner getSectionDesigner();

}