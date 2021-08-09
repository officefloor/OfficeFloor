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

import net.officefloor.compile.impl.structure.CompileContextImpl;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract tests for the {@link CompileContext}.
 *
 * @param <N> {@link Node} type.
 * @param <T> Type.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTestTypeContext<N extends Node, T> extends OfficeFrameTestCase {

	/**
	 * {@link CompileContext} to test.
	 */
	protected CompileContext context = new CompileContextImpl(null);

	/**
	 * Type of {@link Node} to create mocks.
	 */
	private final Class<N> nodeClass;

	/**
	 * Type of the type to create mocks;
	 */
	private final Class<T> typeClass;

	/**
	 * Function to load the type from the {@link Node}.
	 */
	private final ContextTypeLoader<N, T> nodeTypeLoader;

	/**
	 * Function to load the type from the {@link CompileContext}
	 */
	private final ContextTypeLoader<N, T> contextTypeLoader;

	/**
	 * Instantiate.
	 * 
	 * @param nodeClass         Type of {@link Node} to create mocks.
	 * @param typeClass         Type of the type to create mocks.
	 * @param nodeTypeLoader    Function to load the type from the {@link Node}.
	 * @param contextTypeLoader Function to load the type from the
	 *                          {@link CompileContext}
	 */
	public AbstractTestTypeContext(Class<N> nodeClass, Class<T> typeClass, ContextTypeLoader<N, T> nodeTypeLoader,
			ContextTypeLoader<N, T> contextTypeLoader) {
		this.nodeClass = nodeClass;
		this.typeClass = typeClass;
		this.nodeTypeLoader = nodeTypeLoader;
		this.contextTypeLoader = contextTypeLoader;
	}

	/**
	 * Loads the type from the {@link CompileContext}.
	 * 
	 * @param <N> {@link Node} type.
	 * @param <T> Type.
	 */
	protected interface ContextTypeLoader<N, T> {

		/**
		 * Loads the type from the {@link CompileContext}.
		 * 
		 * @param context {@link CompileContext}.
		 * @param node    {@link Node}.
		 * @return Type.
		 */
		T loadType(CompileContext context, N node);
	}

	/**
	 * Ensure can load type.
	 */
	public void testLoadType() {
		T type = this.createMock(this.typeClass);
		N mockNode = this.createMock(this.nodeClass);
		this.recordReturn(mockNode, this.nodeTypeLoader.loadType(this.context, mockNode), type);
		this.replayMockObjects();
		T loadedType = this.contextTypeLoader.loadType(this.context, mockNode);
		this.verifyMockObjects();
		assertSame("Incorrect type", type, loadedType);
	}

	/**
	 * Ensure cache the {@link ManagedObjectType}.
	 */
	public void testCacheManagedObjectType() {
		T type = this.createMock(this.typeClass);
		N mockNode = this.createMock(this.nodeClass);
		T anotherType = this.createMock(this.typeClass);
		N anotherNode = this.createMock(this.nodeClass);
		this.recordReturn(mockNode, this.nodeTypeLoader.loadType(this.context, mockNode), type);
		this.recordReturn(anotherNode, this.nodeTypeLoader.loadType(this.context, anotherNode), anotherType);
		this.replayMockObjects();
		T first = this.contextTypeLoader.loadType(this.context, mockNode);
		T second = this.contextTypeLoader.loadType(this.context, mockNode);
		T third = this.contextTypeLoader.loadType(this.context, anotherNode);
		this.verifyMockObjects();
		assertSame("Incorrect first type", type, first);
		assertSame("Should cache type", first, second);
		assertSame("Should load for another type", anotherType, third);
	}

	/**
	 * Ensure cache {@link ManagedObjectType} not available.
	 */
	public void testCacheTypeNotAvailable() {
		N mockNode = this.createMock(this.nodeClass);
		this.recordReturn(mockNode, this.nodeTypeLoader.loadType(this.context, mockNode), null);
		this.replayMockObjects();
		assertNull("Should not load type", this.contextTypeLoader.loadType(this.context, mockNode));
		assertNull("Should cache type not available", this.contextTypeLoader.loadType(this.context, mockNode));
		this.verifyMockObjects();
	}

}
