package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;

/**
 * {@link ManagingOfficeNode} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagingOfficeNodeImpl implements ManagingOfficeNode {

	/**
	 * Parent {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Initiate.
	 *
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @param context             {@link NodeContext}.
	 */
	public ManagingOfficeNodeImpl(ManagedObjectSourceNode managedObjectSource, NodeContext context) {
		this.managedObjectSourceNode = managedObjectSource;
		this.context = context;
	}

	/*
	 * ======================== Node ===============================
	 */

	@Override
	public String getNodeName() {
		return "MANAGING_OFFICE";
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.managedObjectSourceNode;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ================== LinkOfficeNode ===============================
	 */

	/**
	 * Linked {@link LinkOfficeNode}.
	 */
	private LinkOfficeNode linkedOfficeNode;

	@Override
	public boolean linkOfficeNode(LinkOfficeNode node) {
		return LinkUtil.linkOfficeNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedOfficeNode = link);
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

}