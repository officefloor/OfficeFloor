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

package net.officefloor.compile.impl.type;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;

/**
 * Tests loading the {@link AdministrationType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class AdministrationTypeContextTest extends AbstractTestTypeContext<AdministrationNode, AdministrationType> {

	/**
	 * Instantiate.
	 */
	public AdministrationTypeContextTest() {
		super(AdministrationNode.class, AdministrationType.class,
				(context, node) -> (AdministrationType) node.loadAdministrationType(false),
				(context, node) -> (AdministrationType) context.getOrLoadAdministrationType(node));
	}

}
