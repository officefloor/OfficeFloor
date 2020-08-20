package net.officefloor.compile.impl.state.autowire;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link AutoWireStateManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireStateManagerImpl implements AutoWireStateManager, Node {

	/**
	 * {@link StateManager}.
	 */
	private final StateManager stateManager;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link AutoWirer}.
	 */
	private final AutoWirer<LinkObjectNode> autoWirer;

	/**
	 * Instantiate.
	 * 
	 * @param stateManager {@link StateManager}.
	 * @param officeNode   {@link OfficeNode}.
	 * @param autoWirer    {@link AutoWirer}.
	 */
	public AutoWireStateManagerImpl(StateManager stateManager, OfficeNode officeNode,
			AutoWirer<LinkObjectNode> autoWirer) {
		this.stateManager = stateManager;
		this.officeNode = officeNode;
		this.autoWirer = autoWirer;
	}

	/**
	 * Obtains the {@link ManagedObject} bound name.
	 * 
	 * @param qualifier  Qualifier for the {@link ManagedObject}. May be
	 *                   <code>null</code>.
	 * @param objectType Object type of the {@link ManagedObject}.
	 * @return Bound name of the {@link ManagedObject}.
	 * @throws UnknownObjectException If no {@link ManagedObject} fulfilling the
	 *                                requirement.
	 */
	private String getBoundObjectName(String qualifier, Class<?> objectType) throws UnknownObjectException {

		// Obtain the auto wire link
		AutoWireLink<?, LinkObjectNode>[] links = this.autoWirer.findAutoWireLinks(this,
				new AutoWire(qualifier, objectType));
		if (links.length != 1) {
			throw new UnknownObjectException(
					this.createBinding(qualifier, objectType) + " has " + links.length + " auto wire matches");
		}

		// Obtain the bound node
		LinkObjectNode objectNode = links[0].getTargetNode(this.officeNode);
		BoundManagedObjectNode boundObjectNode = (objectNode instanceof BoundManagedObjectNode)
				? (BoundManagedObjectNode) objectNode
				: LinkUtil.retrieveTarget(objectNode, BoundManagedObjectNode.class, null);

		// Return the bound object node
		return boundObjectNode.getBoundManagedObjectName();
	}

	/**
	 * Creates the binding information for auto-wiring.
	 * 
	 * @param qualifier  Qualifier for the {@link ManagedObject}. May be
	 *                   <code>null</code>.
	 * @param objectType Object type of the {@link ManagedObject}.
	 * @return Binding details.
	 */
	private String createBinding(String qualifier, Class<?> objectType) {
		return (qualifier != null ? qualifier + ":" : "") + objectType.getName();
	}

	/*
	 * ================== AutoWireStateManager ====================
	 */

	@Override
	public <O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user)
			throws UnknownObjectException {

		// Obtain the bound object name
		String boundObjectName = this.getBoundObjectName(qualifier, objectType);

		// Load the object
		try {
			this.stateManager.load(boundObjectName, user);
		} catch (UnknownObjectException ex) {
			// Supplied managed object may not be available
			throw new UnknownObjectException(this.createBinding(qualifier, objectType) + " is not used "
					+ SuppliedManagedObjectSource.class.getSimpleName());
		}
	}

	@Override
	public <O> O getObject(String qualifier, Class<? extends O> objectType, long timeoutInMilliseconds)
			throws UnknownObjectException, Throwable {

		// Obtain the bound object name
		String boundObjectName = this.getBoundObjectName(qualifier, objectType);

		// Obtain the object
		try {
			return this.stateManager.getObject(boundObjectName, timeoutInMilliseconds);
		} catch (UnknownObjectException ex) {
			// Supplied managed object may not be available
			throw new UnknownObjectException(this.createBinding(qualifier, objectType) + " is not used "
					+ SuppliedManagedObjectSource.class.getSimpleName());
		}
	}

	@Override
	public void close() throws Exception {
		this.stateManager.close();
	}

	/*
	 * ========================= Node ====================================
	 */

	@Override
	public String getNodeName() {
		return AutoWireStateManager.class.getSimpleName();
	}

	@Override
	public String getNodeType() {
		return this.getNodeName();
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public boolean isInitialised() {
		return true;
	}

	@Override
	public Node[] getChildNodes() {
		return new Node[0];
	}

}