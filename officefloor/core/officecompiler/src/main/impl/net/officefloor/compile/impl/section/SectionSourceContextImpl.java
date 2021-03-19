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

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.configuration.impl.ConfigurationSourceContextImpl;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SectionSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceContextImpl extends ConfigurationSourceContextImpl implements SectionSourceContext {

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private final String sectionLocation;

	/**
	 * {@link SectionNode}.
	 */
	private final SectionNode sectionNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType      Indicates if loading type.
	 * @param sectionLocation    Location of the {@link SectionNode}.
	 * @param additionalProfiles Additional profiles.
	 * @param propertyList       {@link PropertyList}.
	 * @param sectionNode        Parent {@link SectionNode}.
	 * @param context            {@link NodeContext}.
	 */
	public SectionSourceContextImpl(boolean isLoadingType, String sectionLocation, String[] additionalProfiles,
			PropertyList propertyList, SectionNode sectionNode, NodeContext context) {
		super(sectionNode.getQualifiedName(), isLoadingType, context.getRootSourceContext(), additionalProfiles,
				new PropertyListSourceProperties(propertyList));
		this.sectionLocation = sectionLocation;
		this.sectionNode = sectionNode;
		this.context = context;
	}

	/*
	 * ================= SectionSourceContext ================================
	 */

	@Override
	public String getSectionLocation() {
		return this.sectionLocation;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	public FunctionNamespaceType loadManagedFunctionType(String functionNamespace,
			ManagedFunctionSource managedFunctionSource, PropertyList properties) {
		return CompileUtil.loadType(FunctionNamespaceType.class, managedFunctionSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the function namespace type
					FunctionNamespaceNode namespaceNode = this.context.createFunctionNamespaceNode(functionNamespace,
							this.sectionNode);
					ManagedFunctionLoader managedFunctionLoader = this.context.getManagedFunctionLoader(namespaceNode,
							true);
					return managedFunctionLoader.loadManagedFunctionType(managedFunctionSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FunctionNamespaceType loadManagedFunctionType(String functionNamespace,
			String managedFunctionSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(FunctionNamespaceType.class, managedFunctionSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed function source class
					FunctionNamespaceNode namespaceNode = this.context.createFunctionNamespaceNode(functionNamespace,
							this.sectionNode);
					Class managedFunctionSourceClass = this.context
							.getManagedFunctionSourceClass(managedFunctionSourceClassName, namespaceNode);
					if (managedFunctionSourceClass == null) {
						return null;
					}

					// Load and return the function namespace type
					ManagedFunctionLoader managedFunctionLoader = this.context.getManagedFunctionLoader(namespaceNode,
							true);
					return managedFunctionLoader.loadManagedFunctionType(managedFunctionSourceClass, properties);
				});
	}

	@Override
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the managed object type
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.sectionNode);
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.sectionNode);
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, mosNode);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, properties);
				});
	}

	@Override
	public SectionType loadSectionType(String sectionName, SectionSource sectionSource, String location,
			PropertyList properties) {
		return CompileUtil.loadType(SectionType.class, sectionSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the section type
					SectionLoader sectionLoader = this.context.getSectionLoader(this.sectionNode);
					return sectionLoader.loadSectionType(sectionSource, location, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SectionType loadSectionType(String sectionName, String sectionSourceClassName, String location,
			PropertyList properties) {
		return CompileUtil.loadType(SectionType.class, sectionSourceClassName, this.context.getCompilerIssues(), () -> {

			// Obtain the section source class
			Class sectionSourceClass = this.context.getSectionSourceClass(sectionSourceClassName, this.sectionNode);
			if (sectionSourceClass == null) {
				return null;
			}

			// Load and return the section type
			SectionLoader sectionLoader = this.context.getSectionLoader(this.sectionNode);
			return sectionLoader.loadSectionType(sectionSourceClass, location, properties);
		});
	}

}
