package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;

/**
 * {@link SectionFunctionNamespace} with meta-data for
 * {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionFunctionNamespace {

	/**
	 * {@link SectionFunctionNamespace}.
	 */
	private final SectionFunctionNamespace namespace;

	/**
	 * {@link FunctionNamespaceType}.
	 */
	private final FunctionNamespaceType namespaceType;

	/**
	 * Instantiate.
	 * 
	 * @param namespace     {@link SectionFunctionNamespace}.
	 * @param namespaceType {@link FunctionNamespaceType}.
	 */
	public ClassSectionFunctionNamespace(SectionFunctionNamespace namespace, FunctionNamespaceType namespaceType) {
		this.namespace = namespace;
		this.namespaceType = namespaceType;
	}

	/**
	 * Obtains the {@link SectionFunctionNamespace}.
	 * 
	 * @return {@link SectionFunctionNamespace}.
	 */
	public SectionFunctionNamespace getFunctionNamespace() {
		return namespace;
	}

	/**
	 * Obtains the {@link FunctionNamespaceType}.
	 * 
	 * @return {@link FunctionNamespaceType}.
	 */
	public FunctionNamespaceType getFunctionNamespaceType() {
		return namespaceType;
	}

}