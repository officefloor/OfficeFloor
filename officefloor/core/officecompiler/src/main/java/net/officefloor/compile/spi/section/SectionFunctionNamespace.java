package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyConfigurable;

/**
 * {@link FunctionNamespaceNode} within the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionFunctionNamespace extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SectionFunctionNamespace}.
	 * 
	 * @return Name of this {@link SectionFunctionNamespace}.
	 */
	String getSectionFunctionNamespaceName();

	/**
	 * Adds a {@link SectionFunction}.
	 * 
	 * @param functionName
	 *            Name of the {@link SectionFunction}.
	 * @param functionTypeName
	 *            Name of the {@link ManagedFunctionType} on the
	 *            {@link FunctionNamespaceType}.
	 * @return {@link SectionFunction}.
	 */
	SectionFunction addSectionFunction(String functionName, String functionTypeName);

}