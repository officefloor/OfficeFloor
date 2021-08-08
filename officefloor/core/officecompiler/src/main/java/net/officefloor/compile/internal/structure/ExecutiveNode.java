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

import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * {@link OfficeFloorExecutive} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveNode extends Node, OfficeFloorExecutive {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Executive";

	/**
	 * Initialises the {@link ExecutiveNode}.
	 * 
	 * @param executiveSourceClassName Class name of the {@link ExecutiveSource}.
	 * @param executiveSource          Optional instantiated
	 *                                 {@link ExecutiveSource}. May be
	 *                                 <code>null</code>.
	 */
	void initialise(String executiveSourceClassName, ExecutiveSource executiveSource);

	/**
	 * Loads the {@link ExecutiveType} for the {@link ExecutiveSource}.
	 * 
	 * @return {@link ExecutiveType} or <code>null</code> with issues reported to
	 *         the {@link CompilerIssues}.
	 */
	ExecutiveType loadExecutiveType();

	/**
	 * Sources the {@link Executive}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise,
	 *         <code>false</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceExecutive(CompileContext compileContext);

	/**
	 * Builds the {@link Executive} for this {@link ExecutiveNode}.
	 * 
	 * @param builder        {@link OfficeFloorBuilder}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildExecutive(OfficeFloorBuilder builder, CompileContext compileContext);

}
