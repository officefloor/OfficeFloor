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
import net.officefloor.frame.api.manage.Office;
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
	 * {@link Office}.
	 */
	private final Office office;

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
	 * @param office       {@link Office}.
	 * @param stateManager {@link StateManager}.
	 * @param officeNode   {@link OfficeNode}.
	 * @param autoWirer    {@link AutoWirer}.
	 */
	public AutoWireStateManagerImpl(Office office, StateManager stateManager, OfficeNode officeNode,
			AutoWirer<LinkObjectNode> autoWirer) {
		this.office = office;
		this.stateManager = stateManager;
		this.officeNode = officeNode;
		this.autoWirer = autoWirer;
	}

	/**
	 * Obtains the {@link ManagedObject} bound name.
	 * 
	 * @param qualifier      Qualifier for the {@link ManagedObject}. May be
	 *                       <code>null</code>.
	 * @param objectType     Object type of the {@link ManagedObject}.
	 * @param isThrowUnkonwn Indicates wether to throw
	 *                       {@link UnknownObjectException}.
	 * @return Bound name of the {@link ManagedObject}.
	 * @throws UnknownObjectException If no {@link ManagedObject} fulfilling the
	 *                                requirement.
	 */
	private String getBoundObjectName(String qualifier, Class<?> objectType, boolean isThrowUnkonwn)
			throws UnknownObjectException {

		// Obtain the auto wire link
		AutoWireLink<?, LinkObjectNode>[] links = this.autoWirer.findAutoWireLinks(this,
				new AutoWire(qualifier, objectType));
		if (links.length != 1) {
			return this.handleUnknownBinding(isThrowUnkonwn, qualifier, objectType,
					" has " + links.length + " auto wire matches");
		}

		// Obtain the bound node
		LinkObjectNode objectNode = links[0].getTargetNode(this.officeNode);
		BoundManagedObjectNode boundObjectNode = (objectNode instanceof BoundManagedObjectNode)
				? (BoundManagedObjectNode) objectNode
				: LinkUtil.retrieveTarget(objectNode, BoundManagedObjectNode.class, null);

		// Ensure object actually bound (supplied managed objects may not be used)
		String boundObjectName = boundObjectNode.getBoundManagedObjectName();
		for (String objectName : this.office.getObjectNames()) {
			if (boundObjectName.equals(objectName)) {
				return boundObjectName; // object available
			}
		}

		// As here, the bound object is not available
		return this.handleUnknownBinding(isThrowUnkonwn, qualifier, objectType,
				" is not used " + SuppliedManagedObjectSource.class.getSimpleName());
	}

	/**
	 * Creates the binding information for auto-wiring.
	 * 
	 * @param isThrowUnkonwn Indicates whether to throw
	 *                       {@link UnknownObjectException}. <code>false</code> is
	 *                       typically to check available and avoid throwing
	 *                       {@link UnknownObjectException}.
	 * @param qualifier      Qualifier for the {@link ManagedObject}. May be
	 *                       <code>null</code>.
	 * @param objectType     Object type of the {@link ManagedObject}.
	 * @param messageSuffix  Suffix on the {@link UnknownObjectException} message.
	 * @return <code>null</code> if not throwing {@link UnknownObjectException}.
	 * @throws UnknownObjectException If throwing {@link UnknownObjectException}.
	 */
	private String handleUnknownBinding(boolean isThrowUnkonwn, String qualifier, Class<?> objectType,
			String messageSuffix) throws UnknownObjectException {
		if (isThrowUnkonwn) {
			// Attempting to get object
			throw new UnknownObjectException(
					(qualifier != null ? qualifier + ":" : "") + objectType.getName() + messageSuffix);
		} else {
			// Checking for available
			return null;
		}
	}

	/*
	 * ================== AutoWireStateManager ====================
	 */

	@Override
	public boolean isObjectAvailable(String qualifier, Class<?> objectType) {
		try {
			return this.getBoundObjectName(qualifier, objectType, false) != null;
		} catch (UnknownObjectException ex) {
			throw new IllegalStateException(
					"Should not propagate " + UnknownObjectException.class.getSimpleName() + " on available check");
		}
	}

	@Override
	public <O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user)
			throws UnknownObjectException {

		// Obtain the bound object name
		String boundObjectName = this.getBoundObjectName(qualifier, objectType, true);

		// Load the object
		this.stateManager.load(boundObjectName, user);
	}

	@Override
	public <O> O getObject(String qualifier, Class<? extends O> objectType, long timeoutInMilliseconds)
			throws UnknownObjectException, Throwable {

		// Obtain the bound object name
		String boundObjectName = this.getBoundObjectName(qualifier, objectType, true);

		// Obtain the object
		return this.stateManager.getObject(boundObjectName, timeoutInMilliseconds);
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