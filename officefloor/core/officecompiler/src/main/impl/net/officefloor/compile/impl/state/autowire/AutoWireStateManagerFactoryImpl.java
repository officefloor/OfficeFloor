package net.officefloor.compile.impl.state.autowire;

import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * {@link AutoWireStateManagerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireStateManagerFactoryImpl implements AutoWireStateManagerFactory {

	/**
	 * {@link OfficeFloor}.
	 */
	private final OfficeFloor officeFloor;

	/**
	 * {@link OfficeNode} for the {@link AutoWireStateManager}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link AutoWirer}.
	 */
	private final AutoWirer<LinkObjectNode> autoWirer;

	/**
	 * {@link InternalSupplier} instances.
	 */
	private final InternalSupplier[] internalSuppliers;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * Instantiate.
	 * 
	 * @param officeFloor       {@link OfficeFloor}.
	 * @param officeNode        {@link OfficeNode}.
	 * @param autoWirer         {@link AutoWirer}.
	 * @param internalSuppliers {@link InternalSupplier} instances.
	 * @param monitorClock      {@link MonitorClock}.
	 */
	public AutoWireStateManagerFactoryImpl(OfficeFloor officeFloor, OfficeNode officeNode,
			AutoWirer<LinkObjectNode> autoWirer, InternalSupplier[] internalSuppliers, MonitorClock monitorClock) {
		this.officeFloor = officeFloor;
		this.officeNode = officeNode;
		this.autoWirer = autoWirer;
		this.internalSuppliers = internalSuppliers;
		this.monitorClock = monitorClock;
	}

	/*
	 * =================== AutoWireStateManagerFactory =====================
	 */

	@Override
	public AutoWireStateManager createAutoWireStateManager() {

		// Obtain the Office
		String officeName = this.officeNode.getQualifiedName();
		Office office;
		try {
			office = this.officeFloor.getOffice(officeName);
		} catch (UnknownOfficeException ex) {
			throw new IllegalStateException(
					"Office " + officeName + " not providing " + AutoWirer.class.getSimpleName());
		}

		// Create the state manager
		StateManager stateManager = office.createStateManager();

		// Create and return the auto wire state manager
		return new AutoWireStateManagerImpl(office, stateManager, this.officeNode, autoWirer, this.internalSuppliers,
				this.monitorClock);
	}

}