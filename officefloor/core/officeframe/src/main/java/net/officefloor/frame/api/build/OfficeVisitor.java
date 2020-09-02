package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * Visitor to the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeVisitor {

	/**
	 * Visits the {@link Office}.
	 * 
	 * @param officeMetaData {@link OfficeMetaData} for the {@link Office}.
	 */
	void visit(OfficeMetaData officeMetaData);

}