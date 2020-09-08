package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Visitor for {@link AutoWirer} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWirerVisitor {

	/**
	 * Visits the {@link AutoWirer} for the {@link OfficeNode}.
	 * 
	 * @param officeNode {@link OfficeNode}.
	 * @param autoWirer  {@link AutoWirer} for the {@link OfficeNode}.
	 */
	void visit(OfficeNode officeNode, AutoWirer<LinkObjectNode> autoWirer);

}