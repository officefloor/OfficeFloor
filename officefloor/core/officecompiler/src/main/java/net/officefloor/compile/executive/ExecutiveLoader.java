/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.compile.executive;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;

/**
 * Loads the {@link ExecutiveType} from the {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ExecutiveSourceSpecification} for the {@link ExecutiveSource}.
	 * 
	 * @param                      <TS> {@link ExecutiveSource} type.
	 * @param executiveSourceClass Class of the {@link ExecutiveSource}.
	 * @return {@link PropertyList} of the {@link ExecutiveSourceProperty} instances
	 *         of the {@link ExecutiveSourceSpecification} or <code>null</code> if
	 *         issues, which are reported to the {@link CompilerIssues}.
	 */
	<TS extends ExecutiveSource> PropertyList loadSpecification(Class<TS> executiveSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ExecutiveSourceSpecification} for the {@link ExecutiveSource}.
	 * 
	 * @param executiveSource {@link ExecutiveSource} instance.
	 * @return {@link PropertyList} of the {@link ExecutiveSourceProperty} instances
	 *         of the {@link ExecutiveSourceSpecification} or <code>null</code> if
	 *         issues, which are reported to the {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(ExecutiveSource executiveSource);

	/**
	 * Loads and returns the {@link ExecutiveType} sourced from the
	 * {@link ExecutiveSource}.
	 * 
	 * @param                      <TS> {@link ExecutiveSource} type.
	 * @param executiveName        Name of the {@link Executive}.
	 * @param executiveSourceClass Class of the {@link ExecutiveSource}.
	 * @param propertyList         {@link PropertyList} containing the properties to
	 *                             source the {@link ExecutiveType}.
	 * @return {@link ExecutiveType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<TS extends ExecutiveSource> ExecutiveType loadExecutiveType(String executiveName, Class<TS> executiveSourceClass,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link ExecutiveType} sourced from the
	 * {@link ExecutiveSource}.
	 * 
	 * @param executiveName   Name of the {@link Executive}.
	 * @param executiveSource {@link ExecutiveSource} instance.
	 * @param propertyList    {@link PropertyList} containing the properties to
	 *                        source the {@link ExecutiveType}.
	 * @return {@link ExecutiveType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	ExecutiveType loadExecutiveType(String executiveName, ExecutiveSource executiveSource, PropertyList propertyList);

}