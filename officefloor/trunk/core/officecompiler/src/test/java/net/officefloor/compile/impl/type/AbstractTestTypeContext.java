/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.type;

import java.util.function.Function;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract tests for the {@link TypeContext}.
 *
 * @param <N>
 *            {@link Node} type.
 * @param <T>
 *            Type.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTestTypeContext<N extends Node, T> extends
		OfficeFrameTestCase {

	/**
	 * {@link TypeContext} to test.
	 */
	protected TypeContext context = new TypeContextImpl();

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
	private final Function<N, T> nodeTypeLoader;

	/**
	 * Function to load the type from the {@link TypeContext}
	 */
	private final ContextTypeLoader<N, T> contextTypeLoader;

	/**
	 * Instantiate.
	 * 
	 * @param nodeClass
	 *            Type of {@link Node} to create mocks.
	 * @param typeClass
	 *            Type of the type to create mocks.
	 * @param nodeTypeLoader
	 *            Function to load the type from the {@link Node}.
	 * @param contextTypeLoader
	 *            Function to load the type from the {@link TypeContext}
	 */
	public AbstractTestTypeContext(Class<N> nodeClass, Class<T> typeClass,
			Function<N, T> nodeTypeLoader,
			ContextTypeLoader<N, T> contextTypeLoader) {
		this.nodeClass = nodeClass;
		this.typeClass = typeClass;
		this.nodeTypeLoader = nodeTypeLoader;
		this.contextTypeLoader = contextTypeLoader;
	}

	/**
	 * Loads the type from the {@link TypeContext}.
	 * 
	 * @param <N>
	 *            {@link Node} type.
	 * @param <T>
	 *            Type.
	 */
	protected interface ContextTypeLoader<N, T> {

		/**
		 * Loads the type from the {@link TypeContext}.
		 * 
		 * @param context
		 *            {@link TypeContext}.
		 * @param node
		 *            {@link Node}.
		 * @return Type.
		 */
		T loadType(TypeContext context, N node);
	}

	/**
	 * Ensure can load type.
	 */
	public void testLoadType() {
		T type = this.createMock(this.typeClass);
		N mockNode = this.createMock(this.nodeClass);
		this.recordReturn(mockNode, this.nodeTypeLoader.apply(mockNode), type);
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
		this.recordReturn(mockNode, this.nodeTypeLoader.apply(mockNode), type);
		this.recordReturn(anotherNode, this.nodeTypeLoader.apply(anotherNode),
				anotherType);
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
		this.recordReturn(mockNode, this.nodeTypeLoader.apply(mockNode), null);
		this.replayMockObjects();
		assertNull("Should not load type",
				this.contextTypeLoader.loadType(this.context, mockNode));
		assertNull("Should cache type not available",
				this.contextTypeLoader.loadType(this.context, mockNode));
		this.verifyMockObjects();
	}

}