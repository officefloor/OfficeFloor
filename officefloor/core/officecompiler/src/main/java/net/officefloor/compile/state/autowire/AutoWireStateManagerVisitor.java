package net.officefloor.compile.state.autowire;

import net.officefloor.frame.api.manage.Office;

/**
 * Visitor for the {@link AutoWireStateManager} of each {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManagerVisitor {

	/**
	 * Visits the {@link AutoWireStateManagerFactory} for the {@link Office}.
	 * 
	 * @param officeName                  Name of the {@link Office}.
	 * @param autoWireStateManagerFactory {@link AutoWireStateManagerFactory}.
	 * @throws Exception If fails to visit the {@link AutoWireStateManagerFactory}.
	 */
	void visit(String officeName, AutoWireStateManagerFactory autoWireStateManagerFactory) throws Exception;

}