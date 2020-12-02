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

package net.officefloor.compile.impl.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkExecutionStrategyNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.LinkStartAfterNode;
import net.officefloor.compile.internal.structure.LinkStartBeforeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Utility class to work with links.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkUtil {

	/**
	 * {@link LinkFlowNode} {@link Traverser}.
	 */
	private static final Traverser<LinkFlowNode> FLOW_TRAVERSER = (link) -> link.getLinkedFlowNode();

	/**
	 * {@link LinkObjectNode} {@link Traverser}.
	 */
	private static final Traverser<LinkObjectNode> OBJECT_TRAVERSER = (object) -> object.getLinkedObjectNode();

	/**
	 * {@link LinkTeamNode} {@link Traverser}.
	 */
	private static final Traverser<LinkTeamNode> TEAM_TRAVERSER = (team) -> team.getLinkedTeamNode();

	/**
	 * {@link LinkExecutionStrategyNode} {@link Traverser}.
	 */
	private static final Traverser<LinkExecutionStrategyNode> EXECUTION_STRATEGY_TRAVERSER = (
			executionStrategy) -> executionStrategy.getLinkedExecutionStrategyNode();

	/**
	 * {@link LinkOfficeNode} {@link Traverser}.
	 */
	private static final Traverser<LinkOfficeNode> OFFICE_TRAVERSER = (office) -> office.getLinkedOfficeNode();

	/**
	 * {@link LinkPoolNode} {@link Traverser}.
	 */
	private static final Traverser<LinkPoolNode> POOL_TRAVERSER = (pool) -> pool.getLinkedPoolNode();

	/**
	 * {@link LinkStartBeforeNode} {@link Traveller}.
	 */
	private static final Traveller<LinkStartBeforeNode> START_BEFORE_TRAVELLER = (startBefore) -> startBefore
			.getLinkedStartBeforeNodes();

	/**
	 * {@link LinkStartAfterNode} {@link Traveller}.
	 */
	private static final Traveller<LinkStartAfterNode> START_AFTER_TRAVELLER = (startAfter) -> startAfter
			.getLinkedStartAfterNodes();

	/**
	 * Ensures both inputs are a {@link LinkFlowNode} and if so links them.
	 *
	 * @param linkSource Source {@link LinkFlowNode}.
	 * @param linkTarget Target {@link LinkFlowNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkFlow(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link flow node
		if (!(linkSource instanceof LinkFlowNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link flow node
		if (!(linkTarget instanceof LinkFlowNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkFlowNode) linkSource).linkFlowNode((LinkFlowNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkObjectNode} and if so links them.
	 *
	 * @param linkSource Source {@link LinkObjectNode}.
	 * @param linkTarget Target {@link LinkObjectNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkObject(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link object node
		if (!(linkSource instanceof LinkObjectNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link object node
		if (!(linkTarget instanceof LinkObjectNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkObjectNode) linkSource).linkObjectNode((LinkObjectNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkTeamNode} and if so links them.
	 *
	 * @param linkSource Source {@link LinkTeamNode}.
	 * @param linkTarget Target {@link LinkTeamNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkTeam(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link team node
		if (!(linkSource instanceof LinkTeamNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link team node
		if (!(linkTarget instanceof LinkTeamNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkTeamNode) linkSource).linkTeamNode((LinkTeamNode) linkTarget);
	}

	/**
	 * Ensure both inputs are a {@link LinkExecutionStrategyNode} and if so links
	 * them.
	 * 
	 * @param linkSource Source {@link LinkExecutionStrategyNode}.
	 * @param linkTarget Target {@link LinkExecutionStrategyNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link ExecutionStrategy}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkExecutionStrategy(Object linkSource, Object linkTarget, CompilerIssues issues,
			Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link execution strategy node
		if (!(linkSource instanceof LinkExecutionStrategyNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link execution strategy node
		if (!(linkTarget instanceof LinkExecutionStrategyNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName()) + "]");
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkExecutionStrategyNode) linkSource)
				.linkExecutionStrategyNode((LinkExecutionStrategyNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkOfficeNode} and if so links them.
	 *
	 * @param linkSource Source {@link LinkOfficeNode}.
	 * @param linkTarget Target {@link LinkOfficeNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkOffice(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link office node
		if (!(linkSource instanceof LinkOfficeNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link office node
		if (!(linkTarget instanceof LinkOfficeNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkOfficeNode) linkSource).linkOfficeNode((LinkOfficeNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkPoolNode} and if so links them.
	 * 
	 * @param linkSource Source {@link LinkPoolNode}.
	 * @param linkTarget Target {@link LinkPoolNode}.
	 * @param issues     {@link CompilerIssues}.
	 * @param node       {@link Node} wishing to link the {@link ManagedObjectPool}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkPool(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link pool node
		if (!(linkSource instanceof LinkPoolNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link pool node
		if (!(linkTarget instanceof LinkPoolNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkPoolNode) linkSource).linkPoolNode((LinkPoolNode) linkTarget);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to the
	 * {@link InputManagedObjectNode}.
	 *
	 * @param managedObjectSource {@link ManagedObjectSourceNode}.
	 * @param inputManagedObject  {@link InputManagedObjectNode}.
	 * @param issues              {@link CompilerIssues}.
	 * @param node                {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkManagedObjectSourceInput(Object managedObjectSource, Object inputManagedObject,
			CompilerIssues issues, Node node) {

		// Obtain the node
		if (managedObjectSource instanceof Node) {
			node = (Node) managedObjectSource;
		}

		// Ensure is a managed object source
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + managedObjectSource + " ["
					+ (managedObjectSource == null ? null : managedObjectSource.getClass().getName() + "]"));
			return false; // can not link
		}

		// Ensure is an input managed object node
		if (!(inputManagedObject instanceof InputManagedObjectNode)) {
			issues.addIssue(node, "Invalid input managed object node: " + inputManagedObject + " ["
					+ (inputManagedObject == null ? null : inputManagedObject.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the managed object source to the input managed object
		return ((ManagedObjectSourceNode) managedObjectSource)
				.linkInputManagedObjectNode((InputManagedObjectNode) inputManagedObject);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to start before another
	 * {@link ManagedObjectSourceNode}.
	 *
	 * @param startEarlier {@link ManagedObjectSourceNode} to start earlier.
	 * @param startLater   {@link ManagedObjectSourceNode} to start later.
	 * @param issues       {@link CompilerIssues}.
	 * @param node         {@link Node} wishing to link the start before.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkStartBefore(Object startEarlier, Object startLater, CompilerIssues issues, Node node) {

		// Obtain the node
		if (startEarlier instanceof Node) {
			node = (Node) startEarlier;
		}

		// Ensure start earlier is managed object source
		if (!(startEarlier instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + startEarlier + " ["
					+ (startEarlier == null ? null : startEarlier.getClass().getName() + "]"));
			return false; // can not link
		}

		// Ensure start later is managed object source
		if (!(startLater instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + startLater + " ["
					+ (startLater == null ? null : startLater.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the start before
		return ((ManagedObjectSourceNode) startEarlier).linkStartBeforeNode((ManagedObjectSourceNode) startLater);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to start before
	 * {@link ManagedObject} object type.
	 * 
	 * @param managedObjectSource {@link ManagedObjectSourceNode} to start earlier.
	 * @param managedObjectType   {@link ManagedObject} object type to start later.
	 * @param issues              {@link CompilerIssues}.
	 * @param node                {@link Node} wishing to link the start before.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkAutoWireStartBefore(Object managedObjectSource, String managedObjectType,
			CompilerIssues issues, Node node) {

		// Obtain the node
		if (managedObjectSource instanceof Node) {
			node = (Node) managedObjectSource;
		}

		// Ensure is managed object source
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + managedObjectSource + " ["
					+ (managedObjectSource == null ? null : managedObjectSource.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the auto-wire start before
		return ((ManagedObjectSourceNode) managedObjectSource).linkAutoWireStartBefore(managedObjectType);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to start after another
	 * {@link ManagedObjectSourceNode}.
	 *
	 * @param startLater   {@link ManagedObjectSourceNode} to start later.
	 * @param startEarlier {@link ManagedObjectSourceNode} to start earlier.
	 * @param issues       {@link CompilerIssues}.
	 * @param node         {@link Node} wishing to link the start before.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkStartAfter(Object startLater, Object startEarlier, CompilerIssues issues, Node node) {

		// Obtain the node
		if (startLater instanceof Node) {
			node = (Node) startLater;
		}

		// Ensure start later is managed object source
		if (!(startLater instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + startLater + " ["
					+ (startLater == null ? null : startLater.getClass().getName() + "]"));
			return false; // can not link
		}

		// Ensure start earlier is managed object source
		if (!(startEarlier instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + startEarlier + " ["
					+ (startEarlier == null ? null : startEarlier.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the start after
		return ((ManagedObjectSourceNode) startLater).linkStartAfterNode((ManagedObjectSourceNode) startEarlier);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to start after
	 * {@link ManagedObject} object type.
	 * 
	 * @param managedObjectSource {@link ManagedObjectSourceNode} to start later.
	 * @param managedObjectType   {@link ManagedObject} object type to start
	 *                            earlier.
	 * @param issues              {@link CompilerIssues}.
	 * @param node                {@link Node} wishing to link the start before.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkAutoWireStartAfter(Object managedObjectSource, String managedObjectType,
			CompilerIssues issues, Node node) {

		// Obtain the node
		if (managedObjectSource instanceof Node) {
			node = (Node) managedObjectSource;
		}

		// Ensure is managed object source
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + managedObjectSource + " ["
					+ (managedObjectSource == null ? null : managedObjectSource.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the auto-wire start before
		return ((ManagedObjectSourceNode) managedObjectSource).linkAutoWireStartAfter(managedObjectType);
	}

	/**
	 * Finds the furtherest target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkFlowNode}.
	 * @param targetType Target {@link LinkFlowNode} type to find.
	 * @param issues     {@link CompilerIssues}.
	 * @return Furtherest target {@link LinkFlowNode} or <code>null</code> if no
	 *         targets found.
	 */
	public static <T extends LinkFlowNode> T findFurtherestTarget(LinkFlowNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveFurtherestTarget(link, FLOW_TRAVERSER, targetType, false, issues);
	}

	/**
	 * Finds the furtherest target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkObjectNode}.
	 * @param targetType Target {@link LinkObjectNode} type to find.
	 * @param issues     {@link CompilerIssues}.
	 * @return Furthurest target {@link LinkObjectNode} or <code>null</code> if no
	 *         targets found.
	 */
	public static <T extends LinkObjectNode> T retrieveFurtherestTarget(LinkObjectNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveFurtherestTarget(link, OBJECT_TRAVERSER, targetType, true, issues);
	}

	/**
	 * Finds the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkFlowNode}.
	 * @param targetType Target {@link LinkFlowNode} type to find.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkFlowNode} or <code>null</code> if target not found.
	 */
	public static <T extends LinkFlowNode> T findTarget(LinkFlowNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, FLOW_TRAVERSER, targetType, false, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkFlowNode}.
	 * @param targetType Target {@link LinkFlowNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkFlowNode} or <code>null</code> if issue obtaining
	 *         which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends LinkFlowNode> T retrieveTarget(LinkFlowNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveTarget(link, FLOW_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkObjectNode}.
	 * @param targetType Target {@link LinkObjectNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkObjectNode} or <code>null</code> if issue obtaining
	 *         which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends LinkObjectNode> T retrieveTarget(LinkObjectNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveTarget(link, OBJECT_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Finds the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkTeamNode}.
	 * @param targetType Target {@link LinkTeamNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkTeamNode} or <code>null</code> if not found.
	 */
	public static <T extends Node> T findTarget(LinkTeamNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, TEAM_TRAVERSER, targetType, false, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkExecutionStrategyNode}.
	 * @param targetType Target {@link LinkExecutionStrategyNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkExecutionStrategyNode} or <code>null</code> if
	 *         issue obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends Node> T retrieveTarget(LinkExecutionStrategyNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveTarget(link, EXECUTION_STRATEGY_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkOfficeNode}.
	 * @param targetType Target {@link LinkOfficeNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkOfficeNode} or <code>null</code> if issue obtaining
	 *         which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends Node> T retrieveTarget(LinkOfficeNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, OFFICE_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Finds the target by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkPoolNode}.
	 * @param targetType Target {@link LinkPoolNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkPoolNode} or <code>null</code> if target not found.
	 */
	public static <T extends Node> T findTarget(LinkPoolNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, POOL_TRAVERSER, targetType, false, issues, null).target;
	}

	/**
	 * Finds the targets by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkStartBeforeNode}.
	 * @param targetType Target {@link LinkStartBeforeNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkStartBeforeNode} targets found.
	 */
	public static <T extends Node> T[] findTargets(LinkStartBeforeNode link, Class<T> targetType,
			CompilerIssues issues) {
		return findTargets(link, START_BEFORE_TRAVELLER, targetType, issues);
	}

	/**
	 * Finds the targets by the specified type.
	 * 
	 * @param <T>        Target type.
	 * @param link       Starting {@link LinkStartAfterNode}.
	 * @param targetType Target {@link LinkStartAfterNode} type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Target {@link LinkStartAfterNode} targets found.
	 */
	public static <T extends Node> T[] findTargets(LinkStartAfterNode link, Class<T> targetType,
			CompilerIssues issues) {
		return findTargets(link, START_AFTER_TRAVELLER, targetType, issues);
	}

	/**
	 * Links the {@link LinkFlowNode}.
	 * 
	 * @param node     {@link LinkFlowNode} to have the link loaded.
	 * @param linkNode Link {@link LinkFlowNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link onto the
	 *                 {@link LinkFlowNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkFlowNode(LinkFlowNode node, LinkFlowNode linkNode, CompilerIssues issues,
			Consumer<LinkFlowNode> loader) {
		return linkNode(node, linkNode, FLOW_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkObjectNode}.
	 * 
	 * @param node     {@link LinkObjectNode} to have the link loaded.
	 * @param linkNode Link {@link LinkObjectNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link onto the
	 *                 {@link LinkObjectNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkObjectNode(LinkObjectNode node, LinkObjectNode linkNode, CompilerIssues issues,
			Consumer<LinkObjectNode> loader) {
		return linkNode(node, linkNode, OBJECT_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link AutoWire} {@link LinkObjectNode}.
	 * 
	 * @param node           {@link LinkObjectNode} to have the link loaded.
	 * @param linkNode       Link {@link LinkObjectNode} to load.
	 * @param office         {@link OfficeNode}.
	 * @param autoWirer      {@link AutoWirer} to enable dependencies to be
	 *                       auto-wired.
	 * @param compileContext {@link CompileContext}.
	 * @param issues         {@link CompilerIssues}.
	 * @param loader         {@link Consumer} to load the link onto the
	 *                       {@link LinkObjectNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkAutoWireObjectNode(LinkObjectNode node, LinkObjectNode linkNode, OfficeNode office,
			AutoWirer<LinkObjectNode> autoWirer, CompileContext compileContext, CompilerIssues issues,
			Consumer<LinkObjectNode> loader) {

		// Link the object node
		boolean isLinked = linkObjectNode(node, linkNode, issues, loader);

		// Link managed object source to office
		if (isLinked) {

			// Obtain possible managed object fulfilling object dependency
			ManagedObjectNode managedObject = retrieveTarget(node, OBJECT_TRAVERSER, ManagedObjectNode.class, false,
					issues, null).target;
			if (managedObject != null) {

				// Determine if managed object source is managed by an office
				ManagedObjectSourceNode managedObjectSource = managedObject.getManagedObjectSourceNode();
				if (managedObjectSource != null) {
					managedObjectSource.autoWireToOffice(office, issues);
				}
			}
		}

		// Link dependencies of manage object
		ManagedObjectNode managedObject = retrieveTarget(node, OBJECT_TRAVERSER, ManagedObjectNode.class, false, issues,
				null).target;
		if (managedObject != null) {
			managedObject.autoWireDependencies(autoWirer, office, compileContext);
		}

		// Return whether linked
		return isLinked;
	}

	/**
	 * Loads the {@link AutoWire} instances for the {@link LinkObjectNode} along
	 * with its dependency {@link AutoWire} instances and subsequent (transitive)
	 * dependency {@link AutoWire} instances.
	 * 
	 * @param node           {@link LinkObjectNode} to load transitive dependency
	 *                       {@link AutoWire} instances.
	 * @param allAutoWires   {@link Set} to be loaded with all the {@link AutoWire}
	 *                       instances.
	 * @param compileContext {@link CompileContext}
	 * @param issues         {@link CompilerIssues}.
	 */
	public static void loadAllObjectAutoWires(LinkObjectNode node, Set<AutoWire> allAutoWires,
			CompileContext compileContext, CompilerIssues issues) {
		loadAllObjectAutoWires(node, allAutoWires, compileContext, issues, new HashSet<>());
	}

	/**
	 * Loads the {@link AutoWire} instances.
	 * 
	 * @param node           {@link LinkObjectNode} to load transitive dependency
	 *                       {@link AutoWire} instances.
	 * @param allAutoWires   {@link Set} to be loaded with all the {@link AutoWire}
	 *                       instances.
	 * @param compileContext {@link CompileContext}
	 * @param issues         {@link CompilerIssues}.
	 * @param traversedNodes {@link LinkObjectNode} instances already traversed to
	 *                       avoid cycles (causing infinite loops).
	 */
	private static void loadAllObjectAutoWires(LinkObjectNode node, Set<AutoWire> allAutoWires,
			CompileContext compileContext, CompilerIssues issues, Set<Node> traversedNodes) {

		// Determine if already traversed the node
		if (traversedNodes.contains(node)) {
			return; // break cycle
		}
		traversedNodes.add(node);

		// Handling of managed object
		Consumer<ManagedObjectNode> loadManagedObject = (managedObject) -> {
			// Load auto wires for the managed object
			Arrays.stream(managedObject.getTypeQualifications(compileContext))
					.forEach((typeQualification) -> allAutoWires
							.add(new AutoWire(typeQualification.getQualifier(), typeQualification.getType())));

			// Load the dependency auto wires
			Arrays.stream(managedObject.getManagedObjectDepdendencies())
					.forEach((dependency) -> loadAllObjectAutoWires(dependency, allAutoWires, compileContext, issues,
							traversedNodes));
		};

		// Handling of input managed object
		Consumer<InputManagedObjectNode> loadInputManagedObject = (inputManagedObject) -> {
			// Load auto wires for the input managed object
			Arrays.stream(inputManagedObject.getTypeQualifications(compileContext))
					.forEach((typeQualification) -> allAutoWires
							.add(new AutoWire(typeQualification.getQualifier(), typeQualification.getType())));

			/*
			 * TODO: consider loading the dependency auto wires.
			 * 
			 * As input managed object may be realised by more than one managed object
			 * source, it is possible that this could be a wider spread than anticipated.
			 * Therefore, for now just use the type qualifications of the input managed
			 * object.
			 */
		};

		// Handling of office object
		Consumer<OfficeObjectNode> loadOfficeObject = (officeObject) -> allAutoWires
				.add(new AutoWire(officeObject.getTypeQualifier(), officeObject.getOfficeObjectType()));

		// Determine if managed object
		if (node instanceof ManagedObjectNode) {
			loadManagedObject.accept((ManagedObjectNode) node);

		} else {
			// Attempt to obtain the managed object
			ManagedObjectNode managedObject = retrieveTarget(node, OBJECT_TRAVERSER, ManagedObjectNode.class, false,
					issues, null).target;
			if (managedObject != null) {
				loadManagedObject.accept(managedObject);

			} else {
				// Attempt to obtain input managed object
				InputManagedObjectNode inputManagedObject = retrieveTarget(node, OBJECT_TRAVERSER,
						InputManagedObjectNode.class, false, issues, null).target;
				if (inputManagedObject != null) {
					loadInputManagedObject.accept(inputManagedObject);

				} else {
					// Attempt to load office object
					OfficeObjectNode officeObject = retrieveTarget(node, OBJECT_TRAVERSER, OfficeObjectNode.class,
							false, issues, null).target;
					if (officeObject != null) {
						loadOfficeObject.accept(officeObject);

					} else if (node instanceof OfficeObjectNode) {
						loadOfficeObject.accept((OfficeObjectNode) node);
					}
				}
			}
		}
	}

	/**
	 * Links the {@link LinkObjectNode}.
	 * 
	 * @param node     {@link LinkTeamNode} to have the link loaded.
	 * @param linkNode Link {@link LinkTeamNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link onto the
	 *                 {@link LinkTeamNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkTeamNode(LinkTeamNode node, LinkTeamNode linkNode, CompilerIssues issues,
			Consumer<LinkTeamNode> loader) {
		return linkNode(node, linkNode, TEAM_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkExecutionStrategyNode}.
	 * 
	 * @param node     {@link LinkExecutionStrategyNode} to have the link loaded.
	 * @param linkNode {@link LinkExecutionStrategyNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link onto the
	 *                 {@link LinkExecutionStrategyNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkExecutionStrategyNode(LinkExecutionStrategyNode node, LinkExecutionStrategyNode linkNode,
			CompilerIssues issues, Consumer<LinkExecutionStrategyNode> loader) {
		return linkNode(node, linkNode, EXECUTION_STRATEGY_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkOfficeNode}.
	 * 
	 * @param node     {@link LinkOfficeNode} to have the link loaded.
	 * @param linkNode Link {@link LinkOfficeNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link on the
	 *                 {@link LinkOfficeNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkOfficeNode(LinkOfficeNode node, LinkOfficeNode linkNode, CompilerIssues issues,
			Consumer<LinkOfficeNode> loader) {
		return linkNode(node, linkNode, OFFICE_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkPoolNode}.
	 * 
	 * @param node     {@link LinkPoolNode} to have the link loaded.
	 * @param linkNode Link {@link LinkPoolNode} to load.
	 * @param issues   {@link CompilerIssues}.
	 * @param loader   {@link Consumer} to load the link on the
	 *                 {@link LinkPoolNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkPoolNode(LinkPoolNode node, LinkPoolNode linkNode, CompilerIssues issues,
			Consumer<LinkPoolNode> loader) {
		return linkNode(node, linkNode, POOL_TRAVERSER, issues, loader);
	}

	/**
	 * Loads the link to the {@link Node}.
	 * 
	 * @param node      {@link Node} to have the link loaded.
	 * @param linkNode  Link {@link Node} to load.
	 * @param traverser {@link Traverser}.
	 * @param issues    {@link CompilerIssues}.
	 * @param loader    {@link Consumer} to load the link onto the {@link Node}.
	 * @return <code>true</code> if successfully loaded the link {@link Node}.
	 *         Otherwise, <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	private static <L extends Node> boolean linkNode(L node, L linkNode, Traverser<L> traverser, CompilerIssues issues,
			Consumer<L> loader) {

		// Ensure not already linked
		L existingLink = traverser.getNextLinkNode(node);
		if (existingLink != null) {
			issues.addIssue(node, node.getNodeType() + " " + node.getNodeName() + " linked more than once");
			return false; // already linked
		}

		// Load the link
		loader.accept(linkNode);
		return true;
	}

	/**
	 * Retrieves the furtherest target link by the specified type.
	 * 
	 * @param <L>               Link type.
	 * @param <T>               Target type.
	 * @param link              Starting {@link LinkFlowNode}.
	 * @param traverser         {@link Traverser} to traverse the links.
	 * @param targetType        Target {@link LinkFlowNode} type to find.
	 * @param isIssueOnNoTarget Indicates if issue should be made if target not
	 *                          found.
	 * @param issues            {@link CompilerIssues}.
	 * @return Furtherest target {@link LinkFlowNode} or <code>null</code> if no
	 *         targets found.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> T retrieveFurtherestTarget(L link, Traverser<L> traverser,
			Class<T> targetType, boolean isIssueOnNoTarget, CompilerIssues issues) {

		// Keep track of all traversed links
		Set<Object> traversedLinks = new HashSet<Object>();

		// Find the first target
		T target = retrieveTarget(link, traverser, targetType, isIssueOnNoTarget, issues, traversedLinks).target;

		// Loop to find the furtherest target
		T furtherestTarget = null;
		while (target != null) {
			// Keep reference to found target (will become the furtherest)
			furtherestTarget = target;

			// Attempt to obtain the next target
			link = (L) target;
			Target<T> result = retrieveTarget(link, traverser, targetType, false, issues, traversedLinks);
			if (result.isError) {
				return null;
			}
			target = result.target;
		}

		// Return the furtherest target
		return furtherestTarget;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <L>               Link type.
	 * @param <T>               Target type.
	 * @param link              Starting link.
	 * @param traverser         {@link Traverser} to traverse the links.
	 * @param targetType        Target type to retrieve.
	 * @param isIssueOnNoTarget Indicates if issue should be made if target not
	 *                          found.
	 * @param issues            {@link CompilerIssues}.
	 * @param traversedLinks    Optional traversed links. May be <code>null</code>.
	 * @return Target link or <code>null</code> if issue obtaining which is reported
	 *         to the {@link CompilerIssues}.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> Target<T> retrieveTarget(L link, Traverser<L> traverser,
			Class<T> targetType, boolean isIssueOnNoTarget, CompilerIssues issues, Set<Object> traversedLinks) {

		// Ensure have starting link
		if (link == null) {
			throw new IllegalArgumentException("No starting link to find " + targetType.getSimpleName());
		}

		// Ensure have traversed links
		if (traversedLinks == null) {
			traversedLinks = new HashSet<Object>();
		}

		// Must traverse away from first link
		L previousLink = link;
		link = traverser.getNextLinkNode(link);
		traversedLinks.add(previousLink);

		// Traverse the links until find target type
		while (link != null) {

			// Determine if a cycle
			if (traversedLinks.contains(link)) {
				// In a cycle
				issues.addIssue(previousLink, previousLink.getNodeName() + " results in a cycle on linking to a "
						+ targetType.getSimpleName());
				return new Target<T>(null, true);
			}

			// Keep track for cycles
			traversedLinks.add(link);

			// Determine if link of correct target type
			if (targetType.isInstance(link)) {
				// Found the target
				return new Target<T>((T) link, false);
			}

			// Traverse to next link
			previousLink = link;
			link = traverser.getNextLinkNode(link);
		}

		// Run out of links, so could not find target
		if (isIssueOnNoTarget) {
			issues.addIssue(previousLink, previousLink.getNodeType() + " " + previousLink.getNodeName()
					+ " is not linked to a " + targetType.getSimpleName());
		}
		return new Target<T>(null, false); // target not found
	}

	/**
	 * Finds the targets linked by the specified type.
	 * 
	 * @param <L>        Link type.
	 * @param <T>        Target type.
	 * @param link       Starting link.
	 * @param traveller  {@link Traveller} to travel to all links.
	 * @param targetType Target type to retrieve.
	 * @param issues     {@link CompilerIssues}.
	 * @return Targets.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> T[] findTargets(L link, Traveller<L> traveller, Class<T> targetType,
			CompilerIssues issues) {

		// Ensure have starting link
		if (link == null) {
			throw new IllegalArgumentException("No starting link to find " + targetType.getSimpleName() + " instances");
		}

		// Load the targets
		List<T> targets = new LinkedList<>();
		boolean isSuccessful = loadTargets(link, traveller, targetType, targets, issues, new HashSet<>());

		// Return the targets
		return isSuccessful ? targets.toArray((T[]) Array.newInstance(targetType, targets.size()))
				: (T[]) Array.newInstance(targetType, 0);
	}

	/**
	 * Retrieves the targets linked by the specified type.
	 * 
	 * @param <L>            Link type.
	 * @param <T>            Target type.
	 * @param link           Starting link.
	 * @param traveller      {@link Traveller} to travel to all links.
	 * @param targetType     Target type to retrieve.
	 * @param targets        Listing of targets to be loaded.
	 * @param travelledLinks Optional traversed links. May be <code>null</code>.
	 * @return <code>true</code> if successfully loaded.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> boolean loadTargets(L link, Traveller<L> traveller,
			Class<T> targetType, List<T> targets, CompilerIssues issues, Set<Object> travelledLinks) {

		// Determine if a cycle
		if (travelledLinks.contains(link)) {
			// In a cycle
			issues.addIssue(link,
					link.getNodeName() + " results in a cycle on linking to a " + targetType.getSimpleName());
			return false;
		}

		// Keep track for cycles
		travelledLinks.add(link);

		// Must fan out to all links
		boolean isSuccessful = true;
		for (L linked : traveller.getLinkedNodes(link)) {

			// Determine if link of correct target type
			if (targetType.isInstance(linked)) {
				// Found target
				targets.add((T) linked);

			} else {
				// Load further links
				isSuccessful &= loadTargets(linked, traveller, targetType, targets, issues, travelledLinks);
			}
		}

		// Return whether successful
		return isSuccessful;
	}

	/**
	 * Indicates result of retrieve.
	 */
	private static class Target<T extends Node> {

		/**
		 * Target. May be <code>null</code>.
		 */
		public T target;

		/**
		 * Flag indicating if error in attempting to obtain target.
		 */
		public boolean isError;

		/**
		 * Instantiate.
		 * 
		 * @param target  Target. May be <code>null</code>.
		 * @param isError Flag indicating if error in attempting to obtain target.
		 */
		public Target(T target, boolean isError) {
			this.target = target;
			this.isError = isError;
		}
	}

	/**
	 * Traverser over the links.
	 */
	private static interface Traverser<L> {

		/**
		 * Traverses to the next link.
		 * 
		 * @param link Current link node.
		 * @return Next link node.
		 */
		L getNextLinkNode(L link);
	}

	/**
	 * Travels to all links.
	 */
	private static interface Traveller<L> {

		/**
		 * Travels to all links.
		 * 
		 * @param link Current link node.
		 * @return Linked nodes.
		 */
		L[] getLinkedNodes(L link);
	}

	/**
	 * All access via static methods.
	 */
	private LinkUtil() {
	}

}
