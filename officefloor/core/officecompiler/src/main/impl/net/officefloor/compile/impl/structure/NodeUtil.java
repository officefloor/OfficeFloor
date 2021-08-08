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

package net.officefloor.compile.impl.structure;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.compile.impl.util.CompileUtil;
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
	 * @param          <N> {@link Node} type.
	 * @param nodeName Name of the {@link Node}.
	 * @param nodes    Existing {@link Map} of {@link Node} instances by their
	 *                 names.
	 * @param create   {@link Supplier} to create the {@link Node}.
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
	 * Should the {@link Node} be already initialised, an issue will be reported to
	 * the {@link CompilerIssues}.
	 * 
	 * @param             <N> {@link Node} type.
	 * @param nodeName    Name of the {@link Node}.
	 * @param nodes       {@link Map} of {@link Node} instances by their name.
	 * @param context     {@link NodeContext}.
	 * @param create      {@link Supplier} to create the {@link Node}.
	 * @param initialiser {@link Consumer} to initialise the {@link Node}.
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
	 * Obtains an initialised {@link Node}.
	 * 
	 * @param              <N> {@link Node} type.
	 * @param existingNode Existing {@link Node}. May be <code>null</code>.
	 * @param context      {@link NodeContext}.
	 * @param create       {@link Supplier} to create the {@link Node}.
	 * @param initialiser  {@link Consumer} to initialise the {@link Node}.
	 * @return Initialised {@link Node}.
	 */
	public static <N extends Node> N getInitialisedNode(N existingNode, NodeContext context, Supplier<N> create,
			Consumer<N> initialiser) {

		// Ensure have the node
		N node = existingNode;
		if (node == null) {
			node = create.get();
		}

		// Determine if requires initialising
		if (!node.isInitialised()) {
			// Initialise as not yet initialised
			initialiser.accept(node);
		} else {
			// Node already initialised
			context.getCompilerIssues().addIssue(node, node.getNodeType() + " already configured");
		}

		// Return the node
		return node;
	}

	/**
	 * Initialises the {@link Node}.
	 * 
	 * @param               <S> Type of state.
	 * @param node          {@link Node} to be initialised.
	 * @param context       {@link NodeContext}.
	 * @param existingState Existing initialised state of the {@link Node}. May be
	 *                      <code>null</code>.
	 * @param createState   {@link Supplier} to create the initialised state.
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
	 * Indicates if the {@link Node} tree is fully initialised.
	 * 
	 * @param root   Root {@link Node} of tree.
	 * @param issues {@link CompilerIssues}.
	 * @return <code>true</code> if all {@link Node} instances within the tree are
	 *         initialised. <code>false</code> if non-initialised {@link Node}
	 *         instances within the tree, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	public static boolean isNodeTreeInitialised(Node root, CompilerIssues issues) {
		return isNodeTreeInitialised(root, root, issues);
	}

	/**
	 * Indicates if the {@link Node} tree is fully initialised.
	 * 
	 * @param root   Root {@link Node} of the tree being checked.
	 * @param parent Current {@link Node} of tree being checked.
	 * @param issues {@link CompilerIssues}.
	 * @return <code>true</code> if all {@link Node} instances within the tree are
	 *         initialised. <code>false</code> if non-initialised {@link Node}
	 *         instances within the tree, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	private static boolean isNodeTreeInitialised(Node root, Node parent, CompilerIssues issues) {

		// Determine if parent is initialised
		if (!parent.isInitialised()) {

			// Capture log of tree
			StringWriter log = new StringWriter();
			try {
				logTreeStructure(root, log);
			} catch (IOException ex) {
				// Should not occur as writing to memory
			}

			// Log issue
			issues.addIssue(parent, parent.getNodeType() + " not implemented\n\nTree = " + log.toString() + "\n\n");
			return false;
		}

		// Ensure all child nodes are initialised
		for (Node child : parent.getChildNodes()) {
			if (!isNodeTreeInitialised(root, child, issues)) {
				return false; // child in tree not initialised
			}
		}

		// As here, all of tree is initialised
		return true;
	}

	/**
	 * Logs the {@link Node} tree structure to the {@link Writer}.
	 * 
	 * @param node Root {@link Node} of tree.
	 * @param log  {@link Writer}.
	 * @throws IOException If fails to log.
	 */
	public static void logTreeStructure(Node node, Writer log) throws IOException {
		log.append("{ \"name\": \"" + node.getNodeName() + "\"");
		log.append(", \"type\": \"" + node.getNodeType() + "\"");
		log.append(", \"initialised\": " + String.valueOf(node.isInitialised()));
		Node[] children = node.getChildNodes();
		if (children.length > 0) {
			log.append(", \"children\": [ ");
			boolean isFirst = true;
			for (Node child : children) {
				if (isFirst) {
					isFirst = false;
				} else {
					log.append(", ");
				}
				logTreeStructure(child, log);
			}
			log.append(" ]");
		}
		log.append(" }");
	}

	/**
	 * Obtains the location for the {@link Node}.
	 * 
	 * @param sourceClassName Source {@link Class} name.
	 * @param sourceInstance  Instance of the source. May be <code>null</code>.
	 * @param location        Location of the source.
	 * @return Location for the {@link Node}.
	 */
	public static String getLocation(String sourceClassName, Object sourceInstance, String location) {
		return (sourceInstance != null ? sourceInstance.toString() : sourceClassName) + "(" + location + ")";
	}

	/**
	 * Obtains the child {@link Node} instances.
	 * 
	 * @param children {@link Map} instances containing the child {@link Node}
	 *                 instances.
	 * @return Child {@link Node} instances.
	 */
	@SafeVarargs
	public static Node[] getChildNodes(Map<String, ? extends Node>... children) {

		// Create the listing of children
		final List<Node> childNodes = new ArrayList<>();
		for (final Map<String, ? extends Node> childMap : children) {
			childMap.keySet().stream().sorted((a, b) -> CompileUtil.sortCompare(a, b)).forEach((key) -> {
				childNodes.add(childMap.get(key));
			});
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
