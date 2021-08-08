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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeAdministration} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationNode extends LinkTeamNode, OfficeAdministration {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Administration";

	/**
	 * Initialises this {@link AdministrationNode}.
	 * 
	 * @param administrationSourceClassName Class name of the
	 *                                      {@link AdministrationSource}.
	 * @param administrationSource          Optional instantiated
	 *                                      {@link AdministrationSource}. May be
	 *                                      <code>null</code>.
	 */
	void initialise(String administrationSourceClassName, AdministrationSource<?, ?, ?> administrationSource);

	/**
	 * <p>
	 * Obtains the {@link AdministrationType} for this {@link AdministrationNode}.
	 * <p>
	 * The {@link OfficeAdministration} must be fully populated with the necessary
	 * {@link Property} instances before calling this.
	 * 
	 * @param isLoadingType Indicates using to load type.
	 * @return {@link AdministrationType} for this {@link AdministrationNode}.
	 */
	AdministrationType<?, ?, ?> loadAdministrationType(boolean isLoadingType);

	/**
	 * Sources the {@link Administration}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise,
	 *         <code>false</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceAdministration(CompileContext compileContext);

	/**
	 * Indicates whether to auto-wire {@link ManagedObjectExtensionNode} instances
	 * for {@link Administration}.
	 * 
	 * @return <code>true</code> to auto-wire.
	 */
	boolean isAutoWireAdministration();

	/**
	 * Auto wires the {@link ManagedObjectExtensionNode} for this
	 * {@link Administration}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto wires the {@link Team} for this {@link Administration}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireTeam(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Builds the pre {@link ManagedFunction} {@link Administration}.
	 * 
	 * @param managedFunctionBuilder {@link ManagedFunctionBuilder}.
	 * @param compileContext         {@link CompileContext}.
	 */
	void buildPreFunctionAdministration(ManagedFunctionBuilder<?, ?> managedFunctionBuilder,
			CompileContext compileContext);

	/**
	 * Builds the post {@link ManagedFunction} {@link Administration}.
	 * 
	 * @param managedFunctionBuilder {@link ManagedFunctionBuilder}.
	 * @param compileContext         {@link CompileContext}.
	 */
	void buildPostFunctionAdministration(ManagedFunctionBuilder<?, ?> managedFunctionBuilder,
			CompileContext compileContext);

	/**
	 * Builds the pre-load {@link ManagedObject} {@link Administration}.
	 * 
	 * @param dependencyMappingBuilder {@link DependencyMappingBuilder} for the
	 *                                 {@link ManagedObject}.
	 * @param compileContext           {@link CompileContext}.
	 */
	void buildPreLoadManagedObjectAdministration(DependencyMappingBuilder dependencyMappingBuilder,
			CompileContext compileContext);

}
