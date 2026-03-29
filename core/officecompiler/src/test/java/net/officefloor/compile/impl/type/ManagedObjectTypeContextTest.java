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

import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.managedobject.ManagedObjectType;

/**
 * Tests loading the {@link ManagedObjectType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class ManagedObjectTypeContextTest extends AbstractTestTypeContext<ManagedObjectSourceNode, ManagedObjectType> {

	/**
	 * Instantiate.
	 */
	public ManagedObjectTypeContextTest() {
		super(ManagedObjectSourceNode.class, ManagedObjectType.class,
				(context, node) -> (ManagedObjectType) node.loadManagedObjectType(context),
				(context, node) -> (ManagedObjectType) context.getOrLoadManagedObjectType(node));
	}

}
