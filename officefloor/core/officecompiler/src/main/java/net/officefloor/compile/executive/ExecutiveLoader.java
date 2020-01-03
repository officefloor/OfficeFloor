package net.officefloor.compile.executive;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
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
	 * @param executiveSourceClass Class of the {@link ExecutiveSource}.
	 * @param propertyList         {@link PropertyList} containing the properties to
	 *                             source the {@link ExecutiveType}.
	 * @return {@link ExecutiveType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<TS extends ExecutiveSource> ExecutiveType loadExecutiveType(Class<TS> executiveSourceClass,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link ExecutiveType} sourced from the
	 * {@link ExecutiveSource}.
	 * 
	 * @param executiveSource {@link ExecutiveSource} instance.
	 * @param propertyList    {@link PropertyList} containing the properties to
	 *                        source the {@link ExecutiveType}.
	 * @return {@link ExecutiveType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	ExecutiveType loadExecutiveType(ExecutiveSource executiveSource, PropertyList propertyList);

}