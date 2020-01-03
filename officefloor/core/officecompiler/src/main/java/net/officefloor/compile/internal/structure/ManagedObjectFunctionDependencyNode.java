package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeManagedObjectFunctionDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * {@link ManagedObjectFunctionDependency} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependencyNode
		extends LinkObjectNode, OfficeManagedObjectFunctionDependency, OfficeFloorManagedObjectFunctionDependency {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Function Dependency";

	/**
	 * Initialises the {@link ManagedObjectFunctionDependencyNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link ObjectDependencyType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link ObjectDependencyType} or <code>null</code> with issue reported
	 *         to the {@link CompilerIssues}.
	 */
	ObjectDependencyType loadObjectDependencyType(CompileContext compileContext);

}