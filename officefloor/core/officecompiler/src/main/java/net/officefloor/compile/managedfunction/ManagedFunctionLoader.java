package net.officefloor.compile.managedfunction;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;

/**
 * Loads the {@link FunctionNamespaceType} from the
 * {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param <S>                       {@link ManagedFunctionSource} type.
	 * @param mangedFunctionSourceClass Class of the {@link ManagedFunctionSource}.
	 * @return {@link PropertyList} of the {@link ManagedFunctionSourceProperty}
	 *         instances of the {@link ManagedFunctionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S extends ManagedFunctionSource> PropertyList loadSpecification(Class<S> mangedFunctionSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @return {@link PropertyList} of the {@link ManagedFunctionSourceProperty}
	 *         instances of the {@link ManagedFunctionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(ManagedFunctionSource managedFunctionSource);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param <S>                        {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass Class of the {@link ManagedFunctionSource}.
	 * @param properties                 {@link PropertyList} containing the
	 *                                   properties to source the
	 *                                   {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	<S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(Class<S> managedFunctionSourceClass,
			PropertyList properties);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @param properties            {@link PropertyList} containing the properties
	 *                              to source the {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	FunctionNamespaceType loadManagedFunctionType(ManagedFunctionSource managedFunctionSource, PropertyList properties);

}