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
package net.officefloor.compile.impl.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;

/**
 * Common utility functions for {@link Node} implementations.
 *
 * @author Daniel Sagenschneider
 */
public class NodeUtil {

	/**
	 * Obtains the particular {@link Node}.
	 * 
	 * @param <N>
	 *            {@link Node} type.
	 * @param nodeName
	 *            Name of the {@link Node}.
	 * @param nodes
	 *            Existing {@link Map} of {@link Node} instances by their names.
	 * @param create
	 *            {@link Supplier} to create the {@link Node}.
	 * @return {@link Node} for the name.
	 */
	public static <N extends Node> N getNode(String nodeName, Map<String, N> nodes, Supplier<N> create) {
		N node = nodes.get(nodeName);
		if (node == null) {
			node = create.get();
			nodes.put(nodeName, node);
		}
		return node;
	}

	/**
	 * <p>
	 * Obtains the initialised {@link Node}.
	 * <p>
	 * Should the {@link Node} be already initialised, an issue will be reported
	 * to the {@link CompilerIssues}.
	 * 
	 * @param <N>
	 *            {@link Node} type.
	 * @param nodeName
	 *            Name of the {@link Node}.
	 * @param nodes
	 *            {@link Map} of {@link Node} instances by their name.
	 * @param context
	 *            {@link NodeContext}.
	 * @param create
	 *            {@link Supplier} to create the {@link Node}.
	 * @param initialiser
	 *            {@link Consumer} to initialise the {@link Node}.
	 * @return Initialised {@link Node}.
	 */
	public static <N extends Node> N getInitialisedNode(String nodeName, Map<String, N> nodes, NodeContext context,
			Supplier<N> create, Consumer<N> initialiser) {

		// Obtain the node
		N node = getNode(nodeName, nodes, create);

		// Determine if requires initialising
		if (!node.isInitialised()) {
			// Initialise as not yet initialised
			initialiser.accept(node);
		} else {
			// Node already added and initialised
			context.getCompilerIssues().addIssue(node, node.getNodeType() + " " + nodeName + " already added");
		}

		// Return the node
		return node;

	}

	/**
	 * Initialises the {@link Node}.
	 * 
	 * @param <S>
	 *            Type of state.
	 * @param node
	 *            {@link Node} to be initialised.
	 * @param context
	 *            {@link NodeContext}.
	 * @param existingState
	 *            Existing initialised state of the {@link Node}. May be
	 *            <code>null</code>.
	 * @param createState
	 *            {@link Supplier} to create the initialised state.
	 * @return Initialised state for the {@link Node}.
	 */
	public static <S> S initialise(Node node, NodeContext context, S existingState, Supplier<S> createState) {

		// Ensure not already initialised
		if (existingState != null) {
			context.getCompilerIssues().addIssue(node,
					node.getNodeType() + " " + node.getNodeName() + " already initialised");
			return existingState;
		}

		// Not initialised, so create the initialised state
		return createState.get();
	}

	/**
	 * Obtains the location for the {@link Node}.
	 * 
	 * @param sourceClassName
	 *            Source {@link Class} name.
	 * @param sourceInstance
	 *            Instance of the source. May be <code>null</code>.
	 * @param location
	 *            Location of the source.
	 * @return Location for the {@link Node}.
	 */
	public static String getLocation(String sourceClassName, Object sourceInstance, String location) {
		return (sourceInstance != null ? sourceInstance.toString() : sourceClassName) + "(" + location + ")";
	}

	/**
	 * Obtains the child {@link Node} instances.
	 * 
	 * @param children
	 *            {@link Map} instances containing the child {@link Node}
	 *            instances.
	 * @return Child {@link Node} instances.
	 */
	@SafeVarargs
	public static Node[] getChildNodes(Map<String, ? extends Node>... children) {

		// Create the listing of children
		final List<Node> childNodes = new ArrayList<>();
		for (final Map<String, ? extends Node> childMap : children) {
			childMap.keySet().stream().sorted().forEach((key) -> childNodes.add(childMap.get(key)));
		}

		// Return the children
		return childNodes.toArray(new Node[childNodes.size()]);
	}

	/**
	 * All access via static methods.
	 */
	private NodeUtil() {
	}

}