package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;

/**
 * {@link ManagedObjectDependency} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyNode extends LinkObjectNode, SectionManagedObjectDependency,
		OfficeManagedObjectDependency, OfficeFloorManagedObjectDependency {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Dependency";

	/**
	 * Initialises the {@link ManagedObjectDependencyNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link ObjectDependencyType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ObjectDependencyType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	ObjectDependencyType loadObjectDependencyType(CompileContext compileContext);

}